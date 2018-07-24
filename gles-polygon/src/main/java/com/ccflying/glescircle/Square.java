package com.ccflying.glescircle;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.ccflying.util.Utils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Square implements GLSurfaceView.Renderer {

    private final static String TAG = "Square";

    private String vShader, fShader;

    // 绘制顺序为逆时针
    static float triangleCoords[] = { //
            -0.5f, 0.5f, 0.0f, // top left
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, 0.5f, 0.0f, // top right
            0.5f, -0.5f, 0.0f // bottom right
    };
    // GLES程序id
    private int mProgram;
    // 片元着色器填充颜色
    private float color[] = {1.0f, 1.0f, 1.0f, 1.0f};
    // 顶点坐标buffer，将传递给native层
    private FloatBuffer vertexBuffer;
    // 顶点着色器句柄
    private int mGLPositionHandle;
    // 变换矩阵vMatrix成员句柄
    private int mGLMatrixHandle;
    // 顶点着色器句柄
    private int mGLColorHandle;
    // 顶点偏移量
    private int vertexStride = COORDS_PER_VERTEX * 4;
    // 顶点个数
    private int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    // 每个顶点坐标个数
    private static final int COORDS_PER_VERTEX = 3;

    private float[] mMVPMatrix = new float[16];

    public Square(Context context) {
        vShader = Utils.getAssetsResourceString(context.getResources(),
                "vshader/" + TAG + ".shader");
        fShader = Utils.getAssetsResourceString(context.getResources(),
                "fshader/" + TAG + ".shader");
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        System.out.println(TAG + ": onDrawFrame#");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // 将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        // 获取变换矩阵vMatrix成员句柄
        mGLMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        // 指定vMatrix值
        GLES20.glUniformMatrix4fv(mGLMatrixHandle, 1, false, mMVPMatrix, 0);
        // 获取顶点着色器的vPosition成员句柄
        mGLPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mGLPositionHandle);
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mGLPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        // 获取片元着色器的vColor成员的句柄
        mGLColorHandle = GLES20.glGetUniformLocation(mProgram, "fColor");
        // 设置绘制三角形的颜色
        GLES20.glUniform4fv(mGLColorHandle, 1, color, 0);
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mGLPositionHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        //
        float radio = width * 1f / height;
        float[] mProjectMatrix = new float[16];
        float[] mViewMatrix = new float[16];
        // 设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -radio, radio, -1, 1, 3, 7);
        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, // 相机变换的矩阵, 变换矩阵的起始位置（偏移量）
                0, 0, 6f, // 相机位置
                0, 0, 0, // 观测点位置
                0, 1, 0); // up向量在xyz上的分量
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        System.out.println(TAG + ": onSurfaceCreated#");
        // 将背景设置为灰色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        // 申请底层空间
        vertexBuffer = Utils.allocateFloatBuffer(triangleCoords);
        // 创建应用程序
        mProgram = GLES20.glCreateProgram();
        //
        int vShaderId = Utils.loadShader(GLES20.GL_VERTEX_SHADER, vShader);
        int fShaderId = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, fShader);
        // 将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vShaderId);
        // 将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fShaderId);
        // 连接到着色器程序
        GLES20.glLinkProgram(mProgram);
    }
}
