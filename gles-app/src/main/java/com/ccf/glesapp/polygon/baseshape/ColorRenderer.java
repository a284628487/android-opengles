package com.ccf.glesapp.polygon.baseshape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ColorRenderer implements GLSurfaceView.Renderer {

    final String TAG = "ColorRenderer";

    public ColorRenderer(Context context) {
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(TAG, "onSurfaceCreated: ");
        // 设置背景颜色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "onSurfaceChanged: ");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.e(TAG, "onDrawFrame: ");
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        Random rd = new Random();
        GLES20.glClearColor(rd.nextFloat(), rd.nextFloat(), rd.nextFloat(), 1);
    }
}
