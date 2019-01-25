
**OpenGL**是个专业的3D程序接口，是一个功能强大底层3D图形库，利用了图形加速硬件所以非常高效。
OpenGL的英文全称是“Open Graphics Library”，顾名思义，OpenGL便是“开放的图形程序接口”。
用于渲染2D、3D矢量图形的跨语言、跨平台的应用程序编程接口（API）。

OpenGL ES 1.x 针对固定管线硬件的，OpenGL ES 2.x 针对可编程管线硬件。

**GPU**的软件对外接口主要是**EGL**和**GLES**：

**EGL**

一般图形系统像framebuffer、HIGO、directfb、QT、Android都有自己window、surface等概念，对应的GPU中也有自己的eglWindow、eglSurface。
但是GPU无法直接操作其它平台上的window、surface，所以必须要有EGL作为媒介提供接口，
如将framebuffer提供的window、surface封装成GPU可以识别的eglWindow、eglSurface。

**OpenGL ES**

就是我们熟悉的openles 1.1 和 2.0 接口了，主要负责绘制图形。

OpenGL ES 3.0主要新功能有：
1、渲染管线多重增强，实现先进视觉效果的加速，包括遮挡查询(Occlusion Query)、变缓反馈(Transform Feedback)、实例渲染(Instanced Rendering)、四个或更多渲染目标支持。
2、高质量ETC2/EAC纹理压缩格式成为一项标准功能，不同平台上不再需要需要不同的纹理集。
3、新版GLSL ES 3.0着色语言，全面支持整数和32位浮点操作。
4、纹理功能大幅增强，支持浮点纹理、3D纹理、深度纹理、顶点纹理、NPOT纹理、R/RG单双通道纹理、不可变纹理、2D阵列纹理、无二次幂限制纹理、阴影对比、调配(swizzle)、LOD与mip level clamps、无缝立方体贴图、采样对象、纹理MSAA抗锯齿渲染器。
5、一系列广泛的精确尺寸纹理和渲染缓冲格式，便携移动应用更简单。


GPU绘制图形步骤：先确定图形的顶点，连接线条，再对图形的表面添加纹理或者着色处理。GPU 内部有两个功能处理单元GP（Geometry Processor几何处理单元）、PP（Pixel Processor像素处理单元）分别负责这两步操作。

GPU的对外接口主要是EGL和GLES：

1. EGL：一般图形系统像framebuffer、HIGO、directfb、QT、Android都有自己window、surface等概念，对应的GPU中也有自己的eglWindow、eglSurface，但是GPU无法直接操作其它平台上的window、surface，所以必须要有EGL作为媒介提供接口，如将framebuffer提供的window、surface封装成GPU可以识别的eglWindow、eglSurface。
2. GLES：就是我们熟悉的openles 1.1 和 2.0 接口，主要负责绘制图形。

> 普通的内存 ---(EGL内存封装)--> GPU可绘制的内存。

GPU提供了大规模并行机制，特别适合于执行高度并行的渲染过程，这个“并行”的概念远超出平常在CPU上开的几十个线程，GPU的线程数可以达到上百万个或更多。GPU这种机制很适合用于图像像素的并行处理，大幅提升其计算效率。因此，最终确定将视频增强算法集成到渲染模块，利用GPU进行硬件加速，从而达到视频帧的实时增强处理。
## OpenGL图形渲染管线

OpenGL在渲染处理过程中会顺序执行一系列操作，这一系列相关的处理阶段就被称为OpenGL渲染管线。在OpenGL渲染过程中，一个操作接着一个操作进行，就如流水线作业一样，这样的实现极大地提高了渲染的效率。

OpenGL图形渲染管线，对应操作流程总结为：
顶点数据（Vertices） > 顶点着色器（Vertex Shader） > 图元装配（Assembly） > 光栅化（Rasterization） > 片段着色器（Fragment Shader） > 逐片段处理（Per-Fragment Operations） > 帧缓冲（FrameBuffer）。
再经过双缓冲的交换（SwapBuffer），渲染内容就显示到了屏幕上。其中的每个部分如下：

1. VBO/VAO（顶点缓冲区对象或顶点数组对象）
VBO/VAO是CPU提供给GPU的顶点信息，包括了顶点的位置、颜色（只是顶点的颜色，和纹理的颜色无关）、纹理坐标（用于纹理贴图）等顶点信息。
2. Vertex Shader（顶点着色器）
顶点着色器是处理VBO/VAO提供的顶点信息的程序，实现对顶点的操作，如进行坐标空间转换，计算纹理坐标。
3. Primitive Assembly（图元装配）
顶点着色器下一个阶段是图元装配，图元（Prmitive）是三角形、直线或点等几何对象。这个阶段主要是把顶点着色器输出的顶点组合成图元。
4. Rasterization（光栅化）
光栅化是将图元转化为一组二维片段的过程，这些二维片段代表着可在屏幕上绘制的像素，它包含位置、颜色、纹理坐标等信息。这些值是由图元的顶点信息进行插值计算得到，并送到片元着色器中处理。
5. Fragment Shader（片段着色器）
片段着色器为片段（像素）上的操作实现了通用的可编程方法，光栅化输出的每个片段都会执行一遍片段着色器程序。
6. Per-Fragment Operations（逐片段操作）
在这一阶段中，通过对片元着色器输出的每一个片元进行一系列测试与处理，从而决定最终用于渲染的像素。
7. Framebuffer（帧缓冲区）
这是流水线的最后一个阶段，Framebuffer中存储这可以用于渲染到屏幕或纹理中的像素值。

![xx](http://static.oschina.net/uploads/space/2014/1022/101551_YDWf_219279.png)

OpenGL由**固定管线功能部分**（fixed function stages）和**可编程部分**（programmable stages）组成，可编程部分即着色器（Shader）。上图给出了图形渲染的实例流程，在上述流程中，顶点着色器Vertex Shader和片段着色器Fragment Shader是可编程管线。用户一般可对Vertex Shader和Fragment Shader进行动态编程定制（采用GLSL着色器语言），以实现相应的渲染操作。

## GLSL创建着色器

OpenGL允许创建自己的着色器Shader，且需同时创建两个Shader，分别是Vertex Shader和Fragment Shader。通过着色器语言（OpenGL Shading Language）编写着色器程序在GPU（Graphic Processor Unit）上执行，OpenGL主程序在CPU上执行，主程序（CPU）向显存输入顶点等数据，启动渲染过程，并对渲染过程进行控制。其中，着色器在GPU上执行，代替了固定的渲染管线的一部分，使渲染管线中不同层次具有可编程型。
在OpenGL中，GLSL的Shader使用的流程与C语言相似，每个Shader类似一个C源码模块。首先需要由OpenGL单独编译（Compile），然后将一组编译好的Shader链接（Link）成一个完整程序。下图显示了创建Shader的必要步骤：

![xx](http://hi.csdn.net/attachment/201107/19/0_1311044363779z.gif)

其中，Shader对象与Program对象就相当于编译器与链接器。Shader对象载入源码，然后编译成Object形式（就像C源码编译成 .obj文件）。经过编译的Shader就可以装配到 Program对象中，每个Program对象必须装配两个Shader对象：一个顶点Shader，一个片元 Shader，然后Program对象被链接成“可执行文件”。这样就可以在Render中使用该“可执行文件”进行渲染操作。
因此，通过创建着色器Shader，并将视频增强算法集成到渲染模块的片段着色器中，就可方便地实现视频的增强处理与渲染。

1. OPENGL ES 3.0编程指南.
2. OpenGL ES应用开发实践指南 (Android 卷).
3. OpenGL / OpenGL ES Reference Compiler.
https://www.khronos.org/opengles/sdk/tools/Reference-Compiler/
4. GLSL optimizer. https://github.com/aras-p/glsl-optimizer
5. Graphics Shaders: Theory and Practice (Second Edition).
6. GT工具. http://gt.qq.com/index.html

google: opengl es video texture

https://github.com/izacus/nothing_here/blob/master/source/_posts/2014-03-30-rendering-video-with-opengl-on-android.markdown
