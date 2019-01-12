package com.ccf.glesapp.polygon.threed;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.ccf.glesapp.util.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Cube implements GLSurfaceView.Renderer {
    final String TAG = "Cube";

    // https://blog.csdn.net/junzia/article/details/52820177
    private FloatBuffer vertexBuffer, colorBuffer;
    private ShortBuffer indexBuffer;

    final float[] cubePositions = { //
            -1.0f, 1.0f, 1.0f,// 正面左上0
            -1.0f, -1.0f, 1.0f,// 正面左下1
            1.0f, -1.0f, 1.0f,// 正面右下2
            1.0f, 1.0f, 1.0f,// 正面右上3
            -1.0f, 1.0f, -1.0f,// 背面左上4
            -1.0f, -1.0f, -1.0f,// 背面左下5
            1.0f, -1.0f, -1.0f,// 背面右下6
            1.0f, 1.0f, -1.0f,// 背面右上7
    };

    final short[] indexes = { //
            0, 3, 2, 0, 2, 1, // 正面
            0, 1, 5, 0, 5, 4, // 左面
            0, 7, 3, 0, 4, 7, // 上面
            6, 7, 4, 6, 4, 5, // 后面
            6, 3, 7, 6, 2, 3, // 右面
            6, 5, 1, 6, 1, 2 // 下面
    };

    // 八个顶点的颜色，与顶点坐标一一对应
    float colors[] = { //
            0f, 1f, 0f, 1f, // g
            0f, 1f, 0f, 1f,// g
            0f, 1f, 0f, 1f,// g
            0f, 1f, 0f, 1f,// g
            1f, 0f, 0f, 1f,// r
            0f, 0f, 1f, 1f,// b
            0f, 0f, 1f, 1f,// b
            1f, 0f, 0f, 1f,// r
    };
    // 顶点着色器vPosition句柄
    private int mPositionHandle;
    private int mColorHandle;

    private float[] mMVPMatrix = new float[16];

    private int mMatrixHandler;

    private final static int COORDS_PER_VERTEX = 3;

    // 顶点个数
    private final int vertexCount = cubePositions.length / COORDS_PER_VERTEX;
    // 顶点之间的偏移量
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 每个顶点四个字节

    private int mProgram;

    private String vShader = "uniform mat4 vMatrix;\n" +
            "attribute vec4 vPosition;\n" +
            "varying vec4 fColor;\n" +
            "attribute vec4 vColor;\n" +
            "void main() {\n" +
            "    gl_Position = vMatrix * vPosition;\n" +
            "    fColor = vColor;\n" +
            "}\n";

    private String fShader = "precision mediump float;\n" +
            "varying vec4 fColor;\n" +
            "void main() {\n" +
            "    gl_FragColor = fColor;\n" +
            "}\n";

    public Cube(Context context) {
        vertexBuffer = Utils.allocateFloatBuffer(cubePositions);
        colorBuffer = Utils.allocateFloatBuffer(colors);
        indexBuffer = Utils.allocateShortBuffer(indexes);
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //
        GLES20.glUseProgram(mProgram);
        // 变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        // 指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        // vPosition句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用三角形顶点句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // 添加坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        // 片元着色器vColor句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false,
                0, colorBuffer);
        // 使用索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexes.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        // 计算宽高比
        float ratio = (float) width / height;
        float[] mProjectMatrix = new float[16];
        float[] mViewMatrix = new float[16];
        // 设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix,// 接收相机变换的矩阵
                0, // 变换矩阵的起始位置（偏移量）
                10.0f, 10.0f, 10.0f,// 相机位置 x, y, z
                0f, 0f, 0f, // 观测点位置
                0f, 1.0f, 0.0f); // up向量在xyz上的分量
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        // 开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        //
        int vertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, vShader);
        int fragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, fShader);
        // 创建一个OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        // 添加顶点着色器
        GLES20.glAttachShader(mProgram, vertexShader);
        // 添加片元着色器
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 连接着色器程序
        GLES20.glLinkProgram(mProgram);
    }
}
