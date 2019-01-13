package com.ccf.glesapp.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ccf.glesapp.R;

public class CameraEntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_entry);
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
