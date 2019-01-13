package com.ccf.glesapp.egl.v1;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

public class EGLv1Activity extends AppCompatActivity {

    final static String TAG = "EGLv1Activity";

    private TextureView mTextureView;

    private Renderer mRender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // textureView
        mTextureView = new TextureView(this);
        setContentView(mTextureView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTextureView.isAvailable()) {
            mRender = new Renderer(mTextureView.getSurfaceTexture(), mTextureView.getWidth(), mTextureView.getHeight());
            mRender.start();
        } else {
            // renderer
            mRender = new Renderer();
            mRender.start();
            mTextureView.setSurfaceTextureListener(mRender);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRender.stopRender();
        mRender.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static class Renderer extends Thread implements TextureView.SurfaceTextureListener {

        private EGLHelperV1 mHelper;
        private Object mLock = new Object();
        private boolean isPaused = false;
        private SurfaceTexture mSurfaceTexture;
        //
        private int width;
        //
        private int height;

        public Renderer() {
        }

        public Renderer(SurfaceTexture st, int w, int h) {
            this.mSurfaceTexture = st;
            this.width = w;
            this.height = h;
        }

        @Override
        public void run() {
            super.run();
            while (true) {
                synchronized (mLock) {
                    while (!isPaused && null == mSurfaceTexture) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (isPaused) {
                        break;
                    }
                }
                // 创建EGL环境
                mHelper = new EGLHelperV1();
                // 创建Surface
                Surface sv = new Surface(mSurfaceTexture);
                mHelper.createWindowSurface(sv);
                // 切换GLContext环境
                mHelper.makeCurrent();
                // 绘制Frame
                drawFrame();
                // 释放
                mHelper.release();
                //
                Log.d(TAG, "Renderer released");
            }
        }

        public void stopRender() {
            synchronized (mLock) {
                isPaused = true;
            }
        }

        private void drawFrame() {
            final int BLOCK_WIDTH = 80;
            final int BLOCK_SPEED = 2;
            float clearColor = 0.0f;
            int xpos = -BLOCK_WIDTH / 2;
            int xdir = BLOCK_SPEED;

            Log.d(TAG, "drawFrame on " + width + "x" + height + " EGL surface");

            while (!isPaused) {
                // Check to see if the TextureView's SurfaceTexture is still valid.
                synchronized (mLock) {
                    if (null == mSurfaceTexture) {
                        Log.e(TAG, "drawFrame exiting");
                        return;
                    }
                }
                Log.w(TAG, "drawFrame " + width + "x" + height + " EGL surface");

                // Still alive, render a frame.
                GLES20.glClearColor(clearColor, clearColor, clearColor, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
                GLES20.glScissor(xpos, height / 4, BLOCK_WIDTH, height / 2);
                GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

                mHelper.swapBuffers();

                // Advance state
                clearColor += 0.015625f;
                if (clearColor > 1.0f) {
                    clearColor = 0.0f;
                }
                xpos += xdir;
                if (xpos <= -BLOCK_WIDTH / 2 || xpos >= width - BLOCK_WIDTH / 2) {
                    Log.w(TAG, "change direction");
                    xdir = -xdir;
                }
            }
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureAvailable");
            synchronized (mLock) {
                mSurfaceTexture = surface;
                mLock.notifyAll();
            }
            this.width = width;
            this.height = height;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.e(TAG, "onSurfaceTextureSizeChanged");
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.e(TAG, "onSurfaceTextureDestroyed");
            synchronized (mLock) {
                isPaused = true;
                mSurfaceTexture.release();
                mSurfaceTexture = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }
}
