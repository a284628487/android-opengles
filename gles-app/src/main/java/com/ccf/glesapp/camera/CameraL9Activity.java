package com.ccf.glesapp.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.ccf.glesapp.camera.l9.CameraView;

@SuppressLint("NewApi")
public class CameraL9Activity extends Activity {

    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraView = new CameraView(this);
        setContentView(cameraView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }
}
