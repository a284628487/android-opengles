/*
 *
 * CameraDrawer.java
 *
 * Created by Wuwang on 2016/11/5
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccf.glesapp.camera.l9;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import com.ccf.glesapp.stream.camerafilter.NoFilter;
import com.ccf.glesapp.stream.StreamFilter;
import com.ccf.glesapp.util.Gl2Utils;
import com.ccf.glesapp.util.Utils;

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
        int texture = Utils.createStreamTexture();
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

}
