/*
 *
 * CameraDrawer.java
 *
 * Created by Wuwang on 2016/11/5
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccf.glesapp.camera.l9;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ccf.glesapp.stream.camerafilter.NoFilter;
import com.ccf.glesapp.stream.StreamFilter;
import com.ccf.glesapp.util.Gl2Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Description:
 */
public class CameraRender implements GLSurfaceView.Renderer {

    final String TAG = "CameraRender";

    private float[] matrix = new float[16];
    private SurfaceTexture surfaceTexture;
    private int width, height;
    private int dataWidth, dataHeight;
    private StreamFilter mFilter;

    // private int cameraId = 0;

    public CameraRender(Resources res, boolean back) {
        mFilter = new NoFilter(res, back);
        // mFilter = new GrayFilter(res, back);
    }

    public void setDataSize(int dataWidth, int dataHeight) {
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        calculateMatrix();
    }

    public void setViewSize(int width, int height) {
        this.width = width;
        this.height = height;
        calculateMatrix();
    }

    private void calculateMatrix() {
        matrix = new float[16];
        Gl2Utils.getShowMatrix(matrix, this.dataWidth, this.dataHeight,
                this.width, this.height);
        // matrix = new float[] { //
        // 1, 0, 0, 0,//
        // 0, 0.53f, 0, 0,//
        // 0, 0, -1f, 0, //
        // 0, 0, 1f, 1f //
        // };
        // if (cameraId == 1) {
        // Gl2Utils.flip(matrix, true, false);
        // Gl2Utils.rotate(matrix, -90);
        // } else {
        // Gl2Utils.rotate(matrix, 180);
        // }
        mFilter.setMatrix(matrix);
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public void setCameraId(int id) {
        // this.cameraId = id;
        calculateMatrix();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int texture = createTextureID();
        surfaceTexture = new SurfaceTexture(texture);
        // TODO - Created
        mFilter.setTextureId(texture);
        mFilter.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        setViewSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (surfaceTexture != null) {
            surfaceTexture.updateTexImage();
        }
        mFilter.onDrawFrame();
    }

    @SuppressLint("InlinedApi")
    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
        //
        // 由于我们创建的是扩展纹理，所以绑定的时候我们也需要绑定到扩展纹理上才可以正常使用，
        // GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])。
    }
    // 直接传入buffer数据。
    // GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width,
    // height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
}
