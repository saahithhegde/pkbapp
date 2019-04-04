package sdi.com.pkb;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import sdi.com.pkb.preview.CameraPreviewer;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class MainActivity extends Activity {
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
