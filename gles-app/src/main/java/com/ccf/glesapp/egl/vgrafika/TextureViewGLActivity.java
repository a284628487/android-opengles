/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ccf.glesapp.egl.vgrafika;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;


/**
 * 和GLSurfaceView不同，TextureView并不管理EGLConfig或者渲染线程，所以得由我们自己创建线程来管理。
 * rendering步骤：
 * <p>
 * 1. Render thread draws with GL on its local EGLSurface, a window surface it created.  The
 * window surface is backed by the SurfaceTexture from TextureVIew.
 * 2. The SurfaceTexture takes what is rendered onto it and makes it available as a GL texture.
 * 3. TextureView takes the GL texture and renders it onto its EGLSurface. That EGLSurface
 * is a window surface visible to the compositor.
 * <p>
 * Surface和EGLSurface是相关联的，但它们是完全不同的事物
 * <p>
 * 作为示例，只有当onDestroy()的时候，才停止渲染线程，实际是，应该在onPause()中就停止。
 */
public class TextureViewGLActivity extends Activity {
    private static final String TAG = "TextureViewGLActivity";

    // Experiment with allowing TextureView to release the SurfaceTexture from the callback vs.
    // releasing it explicitly ourselves from the draw loop.  The latter seems to be problematic
    // in 4.4 (KK) -- set the flag to "false", rotate the screen a few times, then check the
    // output of "adb shell ps -t | grep `pid grafika`".
    //
    // Must be static or it'll get reset on every Activity pause/resume.
    private static volatile boolean sReleaseInCallback = true;

    private TextureView mTextureView;
    private Renderer mRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // Start up the Renderer thread.  It'll sleep until the TextureView is ready.
        mRenderer = new Renderer();
        mRenderer.start();

        mTextureView = new TextureView(this);
        setContentView(mTextureView);
        mTextureView.setSurfaceTextureListener(mRenderer);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateControls();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        // Don't do this -- halt the thread in onPause() and wait for it to finish.
        mRenderer.halt();
    }

    /**
     * Updates the UI elements to match current state.
     */
    private void updateControls() {
//        Button toggleRelease = (Button) findViewById(R.id.toggleRelease_button);
//        int id = sReleaseInCallback ?
//                R.string.toggleReleaseCallbackOff : R.string.toggleReleaseCallbackOn;
//        toggleRelease.setText(id);
    }

    /**
     * onClick handler for toggleRelease_button.
     */
    public void clickToggleRelease(View unused) {
        sReleaseInCallback = !sReleaseInCallback;
        updateControls();
    }

    /**
     * Handles GL rendering and SurfaceTexture callbacks.
     * 不创建Looper，所以onSurface###将在主线程调用。
     */
    private static class Renderer extends Thread implements TextureView.SurfaceTextureListener {
        private Object mLock = new Object();        // guards mSurfaceTexture, mDone
        private SurfaceTexture mSurfaceTexture;
        private EglCore mEglCore;
        private boolean mDone;

        public Renderer() {
            super("TextureViewGL Renderer");
        }

        @Override
        public void run() {
            while (true) {
                SurfaceTexture surfaceTexture = null;

                // 等SurfaceTexture可用之后，再继续向下执行渲染操作。
                synchronized (mLock) {
                    while (!mDone && (surfaceTexture = mSurfaceTexture) == null) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ie) {
                            throw new RuntimeException(ie);     // not expected
                        }
                    }
                    if (mDone) {
                        break;
                    }
                }
                Log.d(TAG, "Got surfaceTexture=" + surfaceTexture);

                // 使用SurfaceTexture创建EGL surface. 和SurfaceTexture不在同一线程。
                // SurfaceTexture是作为消费者，可能需要updateTexImage()来更新EGL Surface.
                // 而当前这个线程及EGL环境是生产者(生成Frame)
                mEglCore = new EglCore(null, EglCore.FLAG_TRY_GLES3);// EglCore绑定到当前线程中。
                // 创建WindowSurface。EglCore的flags为FLAG_TRY_GLES3，则为不可录制的EGLSurface.
                WindowSurface windowSurface = new WindowSurface(mEglCore, mSurfaceTexture);
                windowSurface.makeCurrent();

                // Render frames until we're told to stop or the SurfaceTexture is destroyed.
                doAnimation(windowSurface);
                // 释放资源 -> eglDestroySurface
                windowSurface.release();
                // 释放资源 -> 销毁EGLContext环境
                mEglCore.release();
                if (!sReleaseInCallback) {
                    Log.i(TAG, "Releasing SurfaceTexture in renderer thread");
                    // 释放SurfaceTexture
                    surfaceTexture.release();
                }
            }

            Log.d(TAG, "Renderer thread exiting");
        }

        /**
         * Draws updates as fast as the system will allow.
         * <p>
         * In 4.4, with the synchronous buffer queue queue, the frame rate will be limited.
         * In previous (and future) releases, with the async queue, many of the frames we
         * render may be dropped.
         * <p>
         * The correct thing to do here is use Choreographer to schedule frame updates off
         * of vsync, but that's not nearly as much fun.
         */
        private void doAnimation(WindowSurface eglSurface) {
            final int BLOCK_WIDTH = 80;
            final int BLOCK_SPEED = 2;
            float clearColor = 0.0f;
            int xpos = -BLOCK_WIDTH / 2;
            int xdir = BLOCK_SPEED;
            int width = eglSurface.getWidth();
            int height = eglSurface.getHeight();

            Log.d(TAG, "Animating " + width + "x" + height + " EGL surface");

            while (true) {
                // Check to see if the TextureView's SurfaceTexture is still valid.
                synchronized (mLock) {
                    SurfaceTexture surfaceTexture = mSurfaceTexture;
                    if (surfaceTexture == null) {
                        Log.d(TAG, "doAnimation exiting");
                        return;
                    }
                }

                // Still alive, render a frame.
                GLES20.glClearColor(clearColor, clearColor, clearColor, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

                GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
                GLES20.glScissor(xpos, height / 4, BLOCK_WIDTH, height / 2);
                GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                GLES20.glDisable(GLES20.GL_SCISSOR_TEST);

                // Publish the frame.  If we overrun the consumer, frames will be dropped,
                // so on a sufficiently fast device the animation will run at faster than
                // the display refresh rate.
                //
                // If the SurfaceTexture has been destroyed, this will throw an exception.
                eglSurface.swapBuffers();

                // Advance state
                clearColor += 0.015625f;
                if (clearColor > 1.0f) {
                    clearColor = 0.0f;
                }
                xpos += xdir;
                if (xpos <= -BLOCK_WIDTH / 2 || xpos >= width - BLOCK_WIDTH / 2) {
                    Log.d(TAG, "change direction");
                    xdir = -xdir;
                }
            }
        }

        /**
         * Tells the thread to stop running.
         */
        public void halt() {
            synchronized (mLock) {
                mDone = true;
                mLock.notify();
            }
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable(" + width + "x" + height + ")");
            synchronized (mLock) {
                mSurfaceTexture = st;
                mLock.notify();
            }
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged(" + width + "x" + height + ")");
            // TODO: ?
        }

        @Override   // will be called on UI thread
        public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
            Log.d(TAG, "onSurfaceTextureDestroyed");

            // We set the SurfaceTexture reference to null to tell the Renderer thread that
            // it needs to stop.  The renderer might be in the middle of drawing, so we want
            // to return false here so that the caller doesn't try to release the ST out
            // from under us.
            //
            // In theory.
            //
            // In 4.4, the buffer queue was changed to be synchronous, which means we block
            // in dequeueBuffer().  If the renderer has been running flat out and is currently
            // sleeping in eglSwapBuffers(), it's going to be stuck there until somebody
            // tears down the SurfaceTexture.  So we need to tear it down here to ensure
            // that the renderer thread will break.  If we don't, the thread sticks there
            // forever.
            //
            // The only down side to releasing it here is we'll get some complaints in logcat
            // when eglSwapBuffers() fails.
            synchronized (mLock) {
                mSurfaceTexture = null;
            }
            if (sReleaseInCallback) {
                Log.i(TAG, "Allowing TextureView to release SurfaceTexture");
            }
            return sReleaseInCallback;
        }

        @Override   // will be called on UI thread
        public void onSurfaceTextureUpdated(SurfaceTexture st) {
            // Log.d(TAG, "onSurfaceTextureUpdated");
        }
    }
}
