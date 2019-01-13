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

import android.graphics.SurfaceTexture;
import android.view.Surface;

/**
 * Recordable EGL window surface(可录制的EGLSurface)
 * It's good practice to explicitly release() the surface, preferably from a "finally" block.
 */
public class WindowSurface extends EglSurfaceBase {
    // 相关联的Surface
    private Surface mSurface;
    private boolean mReleaseSurface;

    /**
     * 关联native window surface到EGLSurface
     * <p>
     * 设置releaseSurface为true，则当release()被调用的时候，Surface也被release，
     * This is convenient(方便的), but can interfere with framework classes that expect to
     * manage the Surface themselves (e.g. if you release a SurfaceView's Surface, the
     * surfaceDestroyed() callback won't fire).
     */
    public WindowSurface(EglCore eglCore, Surface surface, boolean releaseSurface) {
        super(eglCore);
        createWindowSurface(surface);
        mSurface = surface;
        mReleaseSurface = releaseSurface;
    }

    /**
     * 将SurfaceTexture关联到EGLSurface
     */
    public WindowSurface(EglCore eglCore, SurfaceTexture surfaceTexture) {
        super(eglCore);
        createWindowSurface(surfaceTexture);
    }

    /**
     * 释放关联到 EGL surface 的资源 (and, if configured to do so, with the Surface as well).
     *
     * ！！！Does not require that the surface's EGL context be current.
     */
    public void release() {
        // 先releaseSurface，再release EGL 环境.
        releaseEglSurface();
        if (mSurface != null) {
            if (mReleaseSurface) {
                mSurface.release();
            }
            mSurface = null;
        }
    }

    /**
     * 使用一个新的EGLBase重新创建EGLSurface, 调用者应该先调用releaseEglSurface()释放掉旧的EGLSurface.
     * <p>
     * 当我们希望更新关联到Surface的EGLSurface时使用。
     * For example, if we want to share with a different EGLContext, which can only
     * be done by tearing down and recreating the context.  (That's handled by the caller;
     * this just creates a new EGLSurface for the Surface we were handed earlier.)
     * <p>
     * If the previous EGLSurface isn't fully destroyed, e.g. it's still current on a
     * context somewhere, the create call will fail with complaints from the Surface
     * about already being connected.
     */
    public void recreate(EglCore newEglCore) {
        if (mSurface == null) {
            throw new RuntimeException("not yet implemented for SurfaceTexture");
        }
        mEglCore = newEglCore;          // switch to new context
        createWindowSurface(mSurface);  // create new surface
    }
}
