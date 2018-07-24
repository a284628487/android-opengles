package com.ccflying.glestriangle;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.ccflying.MyGLSurfaceView;

public class TriangleActivity extends AppCompatActivity {

    private MyGLSurfaceView mSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSurfaceView = new MyGLSurfaceView(this);
        mSurfaceView.setEGLContextClientVersion(2);
        // mSurfaceView.setRenderer(new Triangle(this));
        // mSurfaceView.setRenderer(new TriangleColorFull(this));
        mSurfaceView.setRenderer(new TriangleMatrix(this));
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
