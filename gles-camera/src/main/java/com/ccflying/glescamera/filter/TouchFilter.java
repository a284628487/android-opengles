package com.ccflying.glescamera.filter;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

public class TouchFilter extends CameraFilter {

    private final String TAG = "TouchFilter";

    private int mGLTouchHandle;
    private int mGLXYHandle;
    private float[] mXY = new float[0];
    private float uXY = 1.0f;
    private float[] mXYInvalid = new float[]{-10, -10};

    public TouchFilter(Resources mRes, boolean back) {
        super(mRes, back);
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/CameraPreviewTouch.shader",
                "fshader/CameraPreviewTouch.shader");
        mGLTouchHandle = GLES20.glGetAttribLocation(mProgram, "vTouch");
        mGLXYHandle = GLES20.glGetUniformLocation(mProgram, "aXY");
    }

    @Override
    public void onSizeChanged(int width, int height) {
    }

    @Override
    protected void onDraw() {
        super.onDraw();
        GLES20.glUniform1f(mGLXYHandle, uXY);
        GLES20.glEnableVertexAttribArray(mGLTouchHandle);
        if (mXY.length == 0) {
            GLES20.glVertexAttrib2fv(mGLTouchHandle, mXYInvalid, 0);
        } else {
            // Log.e(TAG, "onDraw: " + mXY[0] + " - " + mXY[1]);
            GLES20.glVertexAttrib2fv(mGLTouchHandle, mXY, 0);
        }
        GLES20.glDisableVertexAttribArray(mGLTouchHandle);
    }

    public void setTouchXY(float[] xy, float uXY) {
        this.uXY = uXY;
        this.mXY = xy;
    }

}
