package com.ccflying.gles3dimensional;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ccflying.MyGLSurfaceView;
import com.ccflying.glescircle.Square;

public class ThreeDActivity extends AppCompatActivity {

    private MyGLSurfaceView mSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSurfaceView = new MyGLSurfaceView(this);
        mSurfaceView.setEGLContextClientVersion(2);
        // mSurfaceView.setRenderer(new Cube(this));
        // mSurfaceView.setRenderer(new Oval(this));
        // mSurfaceView.setRenderer(new Cylinder(this));
        mSurfaceView.setRenderer(new Cone(this));
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }
}
