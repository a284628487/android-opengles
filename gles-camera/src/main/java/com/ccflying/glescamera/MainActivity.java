package com.ccflying.glescamera;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions(new String[]
                {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);

        setContentView(R.layout.activity_main);
    }

    public void buttonClick(View view) {
        switch (view.getId()) {
            case R.id.camera_back:
                startActivity(new Intent(this, CameraActivity.class).putExtra("cid", 0));
                break;
            case R.id.camera_front:
                startActivity(new Intent(this, CameraActivity.class).putExtra("cid", 1));
                break;
            case R.id.camera_touch:
                startActivity(new Intent(this, CameraActivity.class)
                        .putExtra("cid", 0)
                        .putExtra("touch", 1));
                break;
            case R.id.camera_l9:
                startActivity(new Intent(this, CameraL9Activity.class));
                break;
            case R.id.camera_sv:
                startActivity(new Intent(this, CameraSVActivity.class));
                break;
            case R.id.camera_tex:
                startActivity(new Intent(this, CameraTexActivity.class));
                break;
        }
    }
}
