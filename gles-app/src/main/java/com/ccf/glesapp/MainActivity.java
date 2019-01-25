package com.ccf.glesapp;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ccf.glesapp.camera.CameraEntryActivity;
import com.ccf.glesapp.camera2.Camera2Activity;
import com.ccf.glesapp.egl.v1.EGLv1Activity;
import com.ccf.glesapp.matrix.MatrixActivity;
import com.ccf.glesapp.mediaeffect.MediaEffectActivity;
import com.ccf.glesapp.polygon.Polygon3DActivity;
import com.ccf.glesapp.polygon.PolygonActivity;
import com.ccf.glesapp.texture.TextureActivity;
import com.ccf.glesapp.video.VideoActivity;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 100);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.base_shape:
                startActivity(new Intent(this, PolygonActivity.class));
                break;
            case R.id.threed_shape:
                startActivity(new Intent(this, Polygon3DActivity.class));
                break;
            case R.id.texture:
                startActivity(new Intent(this, TextureActivity.class));
                break;
            case R.id.camera:
                startActivity(new Intent(this, CameraEntryActivity.class));
                break;
            case R.id.camera2:
                startActivity(new Intent(this, Camera2Activity.class));
                break;
            case R.id.video:
                startActivity(new Intent(this, VideoActivity.class));
                break;
            case R.id.media_effect:
                startActivity(new Intent(this, MediaEffectActivity.class));
                break;
            case R.id.matrix:
                startActivity(new Intent(this, MatrixActivity.class));
                break;
            case R.id.egl:
                // startActivity(new Intent(this, EGLv1Activity.class));
                break;
        }
    }
}
