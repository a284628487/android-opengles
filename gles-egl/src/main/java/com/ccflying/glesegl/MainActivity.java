package com.ccflying.glesegl;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.ccflying.glesegl.v1.EGLHelperV1;
import com.ccflying.glesegl.v1.EGLv1Activity;

public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void clickShow(View v) {
        switch (v.getId()) {
            case R.id.egl_v1:
                startActivity(new Intent(this, EGLv1Activity.class));
                break;
        }
    }
}
