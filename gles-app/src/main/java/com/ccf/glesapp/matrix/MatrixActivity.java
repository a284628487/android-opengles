package com.ccf.glesapp.matrix;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MatrixActivity extends Activity {

    private GLSurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSurfaceView = new GLSurfaceView(this);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(new MatrixRender(mSurfaceView));
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
