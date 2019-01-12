package com.ccf.glesapp.polygon.baseshape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ccf.glesapp.util.Utils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MultiColorTriangle implements GLSurfaceView.Renderer {

    private final static String TAG = "MultiColorRenderer";
    //
    private String vShader = "attribute vec4 vPosition;\n" +
            "varying vec4 fColor;\n" +
            "attribute vec4 vColor;\n" +
            "void main() {\n" +
            "    gl_Position = vPosition;\n" +
            "    fColor = vColor;\n" +
            "}";
    private String fShader = "precision mediump float;\n" +
            "varying vec4 fColor;\n" +
            "void main() {\n" +
            "    gl_FragColor = fColor;\n" +
            "}";
    //
    private int mProgram;
    //
    private float[] mCoordinates = { //
            0f, 0.5f, 0f, //
            -0.5f, -0.5f, 0f, //
            0.5f, -0.5f, 0f
    };

    // RGBA
    private float[] mColors = { //
            0.0f, 1.0f, 0.0f, 1.0f, //
            1.0f, 0.0f, 0.0f, 1.0f, //
            0.0f, 0.0f, 1.0f, 1.0f //
    };

    // 坐标底层数据
    private FloatBuffer mGLPositionBuffer;
    // 颜色底层数据
    private FloatBuffer mGLColorBuffer;
    //
    private int mGLPositionHandle, mGLColorHandle;
    // 每个顶点坐标个数
    private static final int COORDS_PER_VERTEX = 3;
    // 每个颜色值数值个数
    private static final int COLOR_PER_VERTEX = 4;

    // 顶点偏移量，即每个顶点在Buffer中所占的byte位数。
    private int vertexStride = COORDS_PER_VERTEX * 4;
    // 顶点个数
    private int vertexCount = mCoordinates.length / COORDS_PER_VERTEX;

    public MultiColorTriangle(Context context) {
        // 申请底层空间
        mGLPositionBuffer = Utils.allocateFloatBuffer(mCoordinates);
        mGLColorBuffer = Utils.allocateFloatBuffer(mColors);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
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
        mGLColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
        // 传递颜色
        GLES20.glEnableVertexAttribArray(mGLColorHandle);
        // 传递 attribute 类型数值。
        GLES20.glVertexAttribPointer(mGLColorHandle, COLOR_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, mGLColorBuffer);
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mGLPositionHandle);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mGLColorHandle);
    }
}