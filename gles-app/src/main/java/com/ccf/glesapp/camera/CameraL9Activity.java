package com.ccf.glesapp.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import com.ccf.glesapp.camera.l9.CameraView;

@SuppressLint("NewApi")
public class CameraL9Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraView cameraView = new CameraView(this);
        setContentView(cameraView);
    }
}
