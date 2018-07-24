package com.ccflying.glestriangle;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.ccflying.util.Utils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Triangle implements GLSurfaceView.Renderer {

    private final static String TAG = "Triangle";
    //
    private String vShader = "";
    private String fShader = "";
    //
    private int mProgram;
    //
    private float[] mCoordinates = { //
            0f, 0.5f, 0f, //
            -0.5f, -0.5f, 0f, //
            0.5f, -0.5f, 0f
    };

    private float[] mColors = {1.0f, 1.0f, 1.0f, 1.0f};

    // 坐标句柄
    private FloatBuffer mGLPositionBuffer;
    //
    private int mGLPositionHandle, mGLColorHandle;
    // 每个顶点坐标个数
    private static final int COORDS_PER_VERTEX = 3;
    // 顶点偏移量，即每个顶点在Buffer中所占的byte位数。
    private int vertexStride = COORDS_PER_VERTEX * 4;
    // 顶点个数
    private int vertexCount = mCoordinates.length / COORDS_PER_VERTEX;

    public Triangle(Context context) {
        String vShaderPath = "vshader/" + TAG + ".shader";
        String fShaderPath = "fshader/" + TAG + ".shader";
        vShader = Utils.getAssetsResourceString(context.getResources(), vShaderPath);
        fShader = Utils.getAssetsResourceString(context.getResources(), fShaderPath);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        System.out
                .println(TAG + ": onSurfaceCreated#" + Thread.currentThread());
        // 将背景设置为灰色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        // 创建vShader
        int vShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShaderId, vShader);
        GLES20.glCompileShader(vShaderId);
        // 创建fShader
        int fShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShaderId, fShader);
        GLES20.glCompileShader(fShaderId);
        // 创建项目
        mProgram = GLES20.glCreateProgram();
        // 将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vShaderId);
        // 将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fShaderId);
        // 连接到着色器程序
        GLES20.glLinkProgram(mProgram);
        // 申请底层空间
        mGLPositionBuffer = Utils.allocateFloatBuffer(mCoordinates);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        System.out
                .println(TAG + ": onSurfaceChanged#" + Thread.currentThread());
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        System.out.println(TAG + ": onDrawFrame#" + Thread.currentThread());
        //
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // 将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        // 获取顶点着色器的vPosition成员句柄
        mGLPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mGLPositionHandle);
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mGLPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, mGLPositionBuffer);
        // 获取片元着色器的vColor成员的句柄
        mGLColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // 传递颜色
        GLES20.glUniform4fv(mGLColorHandle, 1, mColors, 0);
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mGLPositionHandle);
    }
}
//
// 实现GLSurfaceView的Render，在Render中完成三角形的绘制，具体行为有：
// 加载顶点和片元着色器。
// 确定需要绘制图形的坐标和颜色数据。
// 创建program对象，连接顶点和片元着色器，链接program对象。
// 设置视图窗口(ViewPort)。
// 将坐标数据颜色数据传入OpenGL ES程序中。
// 使颜色缓冲区的内容显示到屏幕上。
//