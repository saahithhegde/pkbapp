package sdi.com.pkb;

import android.Manifest;
import android.app.Activity;
import android.app.AppComponentFactory;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
//import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.amazonaws.mobile.client.AWSMobileClient;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import sdi.com.pkb.preview.CameraPreviewer;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreviewer cameraPreviewer;
    private static final int MY_PERMISSIONS_WRITE_EXT_STORAGE = 1;
    private Camera.PictureCallback pictureCallback = (data, camera) -> {
        File pictureFile = getImageFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
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

    };

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


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_WRITE_EXT_STORAGE);

        }
        mCamera = getCameraInstance();
        cameraPreviewer = new CameraPreviewer(this,mCamera);
        FrameLayout frameLayout = findViewById(R.id.camera);
        frameLayout.addView(cameraPreviewer);
        frameLayout.setOnClickListener((click) -> {
            mCamera.takePicture(null,null,pictureCallback);
            startService(new Intent(getBaseContext(),VehicleVerification.class));
        });
    }


}


// -------------------------This needs to be called to upload the file------------------------------------
    // -----------------------------------  -----------------------------------------

//
//    // KEY and SECRET are gotten when we create an IAM user above
//    //https://grokonez.com/android/uploaddownload-files-images-amazon-s3-android
//    BasicAWSCredentials credentials = new BasicAWSCredentials('AKIAXIPWIYJHPHCDF6HN','DsEi3Xjg9bWucMVzXYRLJ9gEKba/0Y6KsAGoE5W3');
//    AmazonS3Client s3Client = new AmazonS3Client(credentials);
//
//    TransferUtility transferUtility =
//            TransferUtility.builder()
//                    .context(getApplicationContext())
//                    .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
//                    .s3Client(s3Client)
//                    .build();
//
//    // "jsaS3" will be the folder that contains the file
//    TransferObserver uploadObserver =
//            transferUtility.upload("detectrc/" + fileName, file); //file gets uploaded here
//
//uploadObserver.setTransferListener(new TransferListener() {
//
//@Override
//public void onStateChanged(int id, TransferState state) {
//        if (TransferState.COMPLETED == state) {
//        // Handle a completed download.
//        }
//        }
//
//@Override
//public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//        float percentDonef = ((float)bytesCurrent/(float)bytesTotal) * 100;
//        int percentDone = (int)percentDonef;
//        }
//
//@Override
//public void onError(int id, Exception ex) {
//        // Handle errors
//        }
//
//        });
//
//// If your upload does not trigger the onStateChanged method inside your
//// TransferListener, you can directly check the transfer state as shown here.
//        if (TransferState.COMPLETED == uploadObserver.getState()) {
//        // Handle a completed upload.
//        }