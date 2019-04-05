package sdi.com.pkb;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.mobile.client.AWSMobileClient;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import sdi.com.pkb.preview.CameraPreviewer;
import sdi.com.pkb.preview.RcBook;
import sdi.com.pkb.preview.ResultDialog;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Camera mCamera;
    private RcBook mCurrentRcBook;
    private CameraPreviewer cameraPreviewer;
    private ProgressDialog dialog;
    private static final int MY_PERMISSIONS_WRITE_EXT_STORAGE = 1;
    private Camera.PictureCallback pictureCallback = (data, camera) -> {
        File pictureFile = getImageFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d("Main activity", "Error creating media file, check storage permissions");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("Main activity", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Main activity", "Error accessing file: " + e.getMessage());
        }
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAXIPWIYJHPHCDF6HN",
                "DsEi3Xjg9bWucMVzXYRLJ9gEKba/0Y6KsAGoE5W3");
        AmazonS3Client s3Client = new AmazonS3Client(credentials);

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(s3Client)
                        .build();

        // "jsaS3" will be the folder that contains the file
        TransferObserver uploadObserver =
                transferUtility.upload(pictureFile.getName(), pictureFile);

        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    Thread recognition = new Thread(()->{
                        AmazonRekognition rekognitionClient = new AmazonRekognitionClient(credentials);
                        rekognitionClient.setEndpoint("https://rekognition.us-east-2.amazonaws.com");
                        System.out.println(pictureFile.getName());
                        ((AmazonRekognitionClient) rekognitionClient).setSignerRegionOverride("us-east-2");
                        DetectTextRequest request = new DetectTextRequest()
                                .withImage(new Image()
                                        .withS3Object(new S3Object()
                                                .withName(pictureFile.getName())
                                                .withBucket("detect-userfiles-mobilehub-466049087")));
                        DetectTextResult result = rekognitionClient.detectText(request);
                        List<TextDetection> textDetections = result.getTextDetections();
                        StringBuilder buffer = setupDatabaseAndModel(textDetections);
                        try {
                            mCurrentRcBook = performRegexOnString(buffer.toString());
                        } catch (NullPointerException e){
                            Toast.makeText(MainActivity.this, "Detection failed", Toast.LENGTH_SHORT).show();
                        }
                        System.out.println(mCurrentRcBook);
                        if (TransferState.COMPLETED == uploadObserver.getState()) {
                            // Handle a completed upload.
                        }
                    });
                    recognition.start();
                    try {
                        recognition.join();
                        dialog.dismiss();
                        reference.child(mCurrentRcBook.getRegNo()).setValue(mCurrentRcBook);
                        DialogFragment dialogFragment = new ResultDialog();
                        Bundle b = new Bundle();
                        b.putString("name",mCurrentRcBook.getOwnerName());
                        b.putString("address",mCurrentRcBook.getOwnerAddress());
                        b.putString("reg",mCurrentRcBook.getRegNo());
                        b.putString("chassis",mCurrentRcBook.getChassisNo());
                        b.putString("color",mCurrentRcBook.getColor());
                        dialogFragment.setArguments(b);
                        dialogFragment.show(getSupportFragmentManager(),"Result Dialog");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int) percentDonef;
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }
        });

    };

    private RcBook performRegexOnString(String input) {
        RcBook book = new RcBook();
        Pattern pattern = Pattern.compile("([A-Z]{2}[0-9]{2}[A-Z][A-Z]? ?[0-9]{4})");//Registration match
        Matcher matcher = pattern.matcher(input);
        while (matcher.find())
            book.setRegNo(matcher.group());
        pattern = Pattern.compile("([A-Z0-9]{17})");// chassis number
        matcher = pattern.matcher(input);
        while (matcher.find())
            book.setChassisNo(matcher.group());
        pattern = pattern.compile("( [A-Z0-9]{14} )");
        matcher = pattern.matcher(input);
        while (matcher.find())
            book.setEngineNo(matcher.group());
        pattern = pattern.compile("MCYCLE|LMVCAR|HMV|HGMV");
        matcher = pattern.matcher(input);
        while (matcher.find())
            book.setClassType(matcher.group());
        pattern = pattern.compile("BLACK|WHITE|PURPLE|RED|GREEN|BLUE|YELLOW|SILVER|GREY");
        matcher = pattern.matcher(input);
        while (matcher.find())
            book.setColor(matcher.group());
        book.setOwnerName("XXXXXX");
        pattern = pattern.compile("ADDRESS");
        matcher = pattern.matcher(input);
        while (matcher.find())
            book.setOwnerAddress(input.substring(matcher.start())
                .replace("\\|"," "));
        return book;
    }

    private StringBuilder setupDatabaseAndModel(List<TextDetection> textDetections) {
        StringBuilder builder = new StringBuilder();
        for (TextDetection textDetection:textDetections){
            if (textDetection.getType().equals("LINE")) {
                builder.append(textDetection.getDetectedText());
                builder.append("|");
            }
        }
        return builder;
    }

    private File getImageFile(int mediaTypeImage) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "PKB");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (mediaTypeImage == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(mediaTypeImage == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        mCamera.stopPreview();
        mCamera.startPreview();
        return mediaFile;

    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){

        }
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AWSMobileClient.getInstance().initialize(this).execute();
//        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("RC");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXT_STORAGE);
        }
        dialog = new ProgressDialog(this);
        dialog.setTitle("Beginning Scan");
        dialog.setMessage("Give us a moment...");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mCamera = getCameraInstance();
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        parameters.setRotation(90);
        parameters.setZoom(30);
        mCamera.setParameters(parameters);
        cameraPreviewer = new CameraPreviewer(this,mCamera);
        FrameLayout frameLayout = findViewById(R.id.camera);
        frameLayout.addView(cameraPreviewer);
        frameLayout.setOnClickListener((click) -> {
            mCamera.takePicture(null,null,pictureCallback);
            dialog.show();
        });
    }


}