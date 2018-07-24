package com.ccflying.glescamera;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

@SuppressLint("NewApi")
public class CameraSVActivity extends Activity implements Renderer {
    final String TAG = "CameraActivity";
    private SurfaceView mSurfaceView;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSurfaceView = new SurfaceView(this);
        setContentView(mSurfaceView);
        mSurfaceView.getHolder().addCallback(new Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder arg0) {
                stopCamera();
            }

            @Override
            public void surfaceCreated(SurfaceHolder arg0) {
                openCameraAndPreview(null, 0, 0);
            }

            @Override
            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
                                       int arg3) {
            }
        });
    }

    private void openCameraAndPreview(final SurfaceTexture surface,
                                      final int width, final int height) {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            mCamera.startPreview();
            mCamera.setPreviewCallback(new PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (null == mCamera)
                        return;
                    mCamera.addCallbackBuffer(data);
                }
            });
        } catch (IOException ioe) {
            // Something bad happened
        }
    }

    private void stopCamera() {
        if (null == mCamera) {
            return;
        }
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
    }

}
