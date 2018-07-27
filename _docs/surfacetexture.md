## SurfaceTexture

从一个图像流中捕获图像帧作为OpenGL ES纹理。
图像流可以是CameraPreview，或者是视频解码。当指定Camera或MediaPlayer对象的输出目标时，SurfaceTexture可以取代SurfaceHolder，这样将使得从图像流中得到的所有帧都输出到SurfaceTexture对象，而不是用于在设备上显示。
当调用updateTexImage()时，用来创建SurfaceTexture的纹理对象内容被更新为包含图像流中最新的图片。这可能会使得流中的某些帧被跳过。

当对纹理进行采样的时候，应该首先使用getTransformMatrix(float[])查询得到的矩阵来变换纹理坐标。
每次调用updateTexImage()的时候，可能会导致变换矩阵发生变化，因此在纹理图像更新时需要重新查询。
该矩阵将传统的2D OpenGL ES纹理坐标列向量(s,t,0,1)，其中s，t∈[0,1]，变换为纹理中对应的采样位置。
该变换补偿了图像流中任何可能导致与传统OpenGL ES纹理有差异的属性。
例如，从图像的左下角开始采样，可以通过使用查询得到的矩阵来变换列向量(0,0,0,1)，而从右上角采样可以通过变换(1,1,0,1)来得到。

纹理对象使用GL_TEXTURE_EXTERNAL_OES作为纹理目标，其是OpenGL ES扩展GL_OES_EGL_image_external定义的。
这种纹理目标会对纹理的使用方式造成一些限制。每次纹理绑定的时候，都要绑定到GL_TEXTURE_EXTERNAL_OES，而不是GL_TEXTURE_2D。
而且，任何需要从纹理中采样的OpenGL ES 2.0 shader都需要声明其对此扩展的使用。
例如，使用指令”#extension GL_OES_EGL_image_external:require”。这些shader也必须使用samplerExternalOES采样方式来访问纹理。

SurfaceTexture对象可以在任何线程里创建。updateTexImage()只能在包含纹理对象的OpenGL ES上下文所在的线程里调用。
可以得到帧信息的回调可以在任何线程被调用，因此在没有做必要的保护的情况下，updateTexImage()不应该直接从回调函数中调用。

## methods

- void setOnFrameAvailableListener(SurfaceTexture.OnFrameAvailableListener l)
设置监听器，当新一帧图像对SurfaceTexture可用时调用

- void updateTexImage()
更新纹理图像为从图像流中提取的最近一帧。

- void getTransformMatrix (float[] mtx)
提取最近调用的updateTexImage()为纹理图像设置的4×4的纹理坐标变换矩阵。该变换矩阵将2D的其次纹理坐标(s,t,0,1)，s，t∈[0,1]变换为对应的用于从纹理中采样的纹理坐标。
在本变换的范围之外对纹理进行采样时未定义的行为。矩阵列主序存储，可以通过glLoadMatrixf()或glUniformMatrix4fv()函数直接传递给OpenGL ES。

[Link](https://developer.android.com/reference/android/graphics/SurfaceTexture)
