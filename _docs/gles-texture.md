
## 纹理初始化
1. glGenTextures()
生成纹理id，可以一次生成多个，后续操作纹理全靠这个id

2. glBindTexture()
操作纹理，传入纹理id作为参数，每次bind之后，后续操作的纹理都是该纹理

3. glTexParameterf()
指定纹理格式。这里包括纹理横向和纵向的重复方式
GL_TEXTURE_WRAP_S
GL_TEXTURE_WRAP_T
和纹理在放大和缩小（同样纹理离远和离近）时的处理，这种设置主要是为了避免同一个纹理反复使用时，远处的纹理反而比近处的清晰
GL_TEXTURE_MAG_FILTER
GL_TEXTURE_MIN_FILTER

4. GLUtils.texImage2D()
给纹理传入图像数据，至此，此纹理相关设置已经结束。后续想使用或者操作这个纹理，只要再glBindTexture这个纹理的id即可.

```java
int[] texture = new int[1];
// 生成纹理(GLsizei n, textures, offset)
GLES20.glGenTextures(1, texture, 0);
// 生成纹理(target, texture)
GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
// 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
// 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
// 设置环绕方向S，截取纹理坐标到[1/2n, 1-1/2n]。将导致永远不会与border融合
GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
// 设置环绕方向T，截取纹理坐标到[1/2n, 1-1/2n]。将导致永远不会与border融合
GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
// 根据以上指定的参数，生成一个2D纹理
GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

```

## 纹理单元
OpenGL可以同时使用多个纹理，这些纹理可以分别绑定到特定的纹理单元。每个纹理单元有其独立的图像、Filter参数等。

`void glActiveTexture(GLenum texUnit)`激活对应的纹理单元，之后所有的纹理操作都是针对这个纹理单元。参数GL_TEXTUREi，i取值0至k，最大值跟硬件相关。

`glUniform1i()` 从0开始递增。

绑定纹理到纹理单元举例:

```java
glActiveTexture(GL_TEXTURE0);
glBindTexture(GL_TEXTURE2D, tex0);
glUniform1i(mTexture1Handle, 0); // 设置纹理句柄

glActiveTexture(GL_TEXTURE1);
glBindTexture(GL_TEXTURE2D, tex1);
glUniform1i(mTexture2Handle, 1);
```

此时GL_TEXTURE0(默认纹理单元)下绑定的是tex0，GL_TEXTURE3下绑定的是tex1
使用时的语句完全相同。在激活和绑定其他纹理之前，使用的都是该纹理单元和纹理。

## 多重纹理与shader

1. fragmentshader.glsl
shader中的fragment.glsl中应该包括多个纹理的采样器

```java
uniform sampler2D Map1;
uniform sampler2D Map2;
```
2. 在代码中获取到shader中改sampler的句柄
相当于建立起了代码和shader之间的通道，通过句柄可以给shader中的sampler赋值

```java
mMap1= GLES20.glGetUniformLocation(Program, "Map1");
mMap2= GLES20.glGetUniformLocation(Program, "Map2");
```

3. 设置纹理单元与shader中sampler的关系
通过赋值，可以指定sampler与纹理单元的关系，想让sampler对哪个纹理单元GL_TEXTUREi中的纹理进行采样/处理，就给它赋值i，如果纹理是GL_TEXTURE0，就给sampler2D赋值为0，以此类推。

```java
GLES20.glUniform1i(mMap1, 0);
GLES20.glUniform1i(mMap2, 1);
```

这里指定完毕之后，后续shader中的Map1的采样对象就是GL_TEXTURE0上绑定的纹理，而Map2的处理对象就是GL_TEXTURE1上绑定的纹理

4. 绑定目标纹理
要使用相应的纹理，步骤和“2.纹理单元”中描述的完全一样（前提是这些纹理以已经完成了初始化），就是：

```java
glActiveTexture(GL_TEXTURE0);
glBindTexture(GL_TEXTURE2D, tex1);
glActiveTexture(GL_TEXTURE1);
glBindTexture(GL_TEXTURE2D, tex2);
```
此时GL_TEXTURE0上绑定的纹理是tex1，对应在shader中的sampler是Map1；GL_TEXTURE1上绑定的纹理是tex2，对应在shader中的sampler是Map2



[opengles2.0中的纹理](http://blog.csdn.net/ldpxxx/article/details/9253603)
[gles官方文档](https://www.khronos.org/opengles/sdk/docs/man/)
[OpenGL ES2.0 生成Mipmap纹理](http://xiaxveliang.blog.163.com/blog/static/297080342013467552467/)
[Link1](https://blog.csdn.net/prahs/article/details/49818345)
[Link2](https://blog.csdn.net/keen_zuxwang/article/details/78362058?locationNum=3&fps=1)
