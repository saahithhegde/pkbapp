package sdi.com.pkb;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.FrameLayout;

import sdi.com.pkb.preview.CameraPreviewer;

public class MainActivity extends Activity {
    private Camera mCamera;
    private CameraPreviewer cameraPreviewer;
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
        mCamera = getCameraInstance();
        cameraPreviewer = new CameraPreviewer(this,mCamera);
        FrameLayout frameLayout = findViewById(R.id.camera);
        frameLayout.addView(cameraPreviewer);
    }
}
