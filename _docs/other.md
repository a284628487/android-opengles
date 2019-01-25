## **Surface**
Handle onto a raw buffer that is being managed by the screen compositor.

## **SurfaceHolder**
Abstract interface to someone holding a display surface.  
Allows you to control the surface size and format, edit the pixels in the surface, 
and monitor changes to the surface. 
This interface is typically available through the {@link SurfaceView} class.

## **SurfaceTexture**

Captures frames from an image stream as an OpenGL ES texture.
(将图像流中的帧捕获为OpenGL ES纹理)

The image stream may come from either camera preview or video decode. 
A {@link android.view.Surface} created from a SurfaceTexture can be used as an output
destination for the 

    {@link android.hardware.camera2},
    {@link android.media.MediaCodec},
    {@link android.media.MediaPlayer},
    {@link android.renderscript.Allocation} APIs.
    
When {@link #updateTexImage} is called, the contents of the texture object specified
when the SurfaceTexture was created are updated to contain the most recent image from the image
stream.  This may cause some frames of the stream to be skipped.

## **TextureView**

A TextureView can be used to display a content stream. Such a content
stream can for instance be a video or an OpenGL scene. The content stream
can come from the application's process as well as a remote process.

TextureView can only be used in a hardware accelerated window. When
rendered in software, TextureView will draw nothing.

Unlike {@link SurfaceView}, TextureView does not create a separate
window but behaves as a regular View. This key difference allows a
TextureView to be moved, transformed, animated, etc. For instance, you
can make a TextureView semi-translucent by calling
<code>myView.setAlpha(0.5f)</code>.</p>

## EGL

    /**
     * 准备EGL环境，需要一个ELGS 2.0的Context，和支持Recording的Surface
     * step:
     * 1. EGL14.eglGetDisplay
     * 2. EGL14.eglInitialize
     * 3. EGL14.eglChooseConfig
     * 4. EGL14.eglCreateContext
     * 5. EGL14.eglCreateWindowSurface
     