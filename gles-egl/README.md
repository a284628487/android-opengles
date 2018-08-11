Create EGL Environment by EGL api.
==================================

## Step

1. 初始化**EGLDisplay**，两个函数**eglGetDisplay**和**eglInitialize**。

```Java
mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

int[] version = new int[2];
EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1);
```

2. 初始化**EGLConfig**，需要先创建attribute配置表，通过**eglChooseConfig**创建。

```Java
// Configure EGL for recording and OpenGL ES 2.0.
int[] attribList = {
        EGL14.EGL_RED_SIZE, 8,
        EGL14.EGL_GREEN_SIZE, 8,
        EGL14.EGL_BLUE_SIZE, 8,
        EGL14.EGL_ALPHA_SIZE, 8,
        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
        EGL_RECORDABLE_ANDROID, 1,
        EGL14.EGL_NONE
};
EGLConfig[] configs = new EGLConfig[1];
int[] numConfigs = new int[1];
if (!EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0,
        configs.length, numConfigs, 0)) {
    throw new RuntimeException("unable to chooseConfig EGL14");
}
```

3. 初始化**EGLContext**，根据上两步创建出的**EGLDisplay**和**EGLConfig**，和设定好的EGL_Version来进行创建。

```Java
mEGLConfig = configs[0];
int[] attrib2_list = {
        EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
        EGL14.EGL_NONE
};
mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mEGLConfig, EGL14.EGL_NO_CONTEXT, attrib2_list, 0);
```

4. 创建**EGLSurface**，将外部的Surface关联到EGLSurface。

```Java
int[] surfaceAttribs = {
        EGL14.EGL_NONE
};
mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, mSurface, surfaceAttribs, 0);
```

5. **eglMakeCurrent**设置之前创建的EGLContext为当前的EGLContext环境。

```Java
EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
```

6. 将**EGLSurface**上绘制的内容`publish`到**EGLDisplay**

```Java
EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
```

7. `release()`，销毁资源

```Java
public void release() {
    if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
        EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
        EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
        EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
        EGL14.eglReleaseThread();
        EGL14.eglTerminate(mEGLDisplay);
    }
    //
    mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    mEGLContext = EGL14.EGL_NO_CONTEXT;
    mEGLSurface = EGL14.EGL_NO_SURFACE;
}
```
