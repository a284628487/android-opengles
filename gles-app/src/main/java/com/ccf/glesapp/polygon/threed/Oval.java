/*
 *
 */
package com.ccf.glesapp.polygon.threed;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.ccf.glesapp.util.Utils;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Description: 圆 / 椭圆
 */
public class Oval implements GLSurfaceView.Renderer {

    private final static String TAG = "Oval";

    private FloatBuffer vertexBuffer;

    private int mProgram;

    private String vShader = "uniform mat4 vMatrix;\n" +
            "attribute vec4 vPosition;\n" +
            "void main() {\n" +
            "    gl_Position = vMatrix * vPosition;\n" +
            "}\n";

    private String fShader = "precision mediump float;\n" +
            "uniform vec4 fColor;\n" +
            "void main() {\n" +
            "    gl_FragColor = fColor;\n" +
            "}";

    static final int COORDS_PER_VERTEX = 3;

    private int mPositionHandle;
    private int mColorHandle;

    private float[] mMVPMatrix = new float[16];

    // 顶点之间的偏移量
    private final int vertexStride = 0; // 每个顶点四个字节

    private int mMatrixHandler;

    private float radius = 1f;
    private int n = 360; // 切割份数

    private float[] shapePos;

    private float height = 0.0f;
    private float yScale;

    // 设置颜色，依次为红绿蓝和透明通道
    float color[] = {1.0f, 1.0f, 1.0f, 1.0f};

    private boolean needClear = true;

    public Oval(Context context) {
        this(context, 0.0f, 0.75f);
    }

    public Oval(Context context, float height, float yScale) {
        this.height = height;
        this.yScale = yScale;

        shapePos = createPositions();

        // 创建vertexBuffer
        vertexBuffer = Utils.allocateFloatBuffer(shapePos);
    }

    private float[] createPositions() {
        ArrayList<Float> data = new ArrayList<>();
        data.add(0.0f); // 设置圆心坐标
        data.add(0.0f);
        data.add(height);
        //
        float angDegSpan = 360f / n;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            data.add((float) (radius * Math.sin(i * Math.PI / 180f))); // x
            data.add((float) (radius * yScale * Math.cos(i * Math.PI / 180f))); // y
            data.add(height); // z
        }
        float[] f = new float[data.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
        }
        return f;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (needClear) {
            // 将背景设置为灰色
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        }
        int vertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, vShader);
        int fragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, fShader);
        // 创建一个空的OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        // 将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vertexShader);
        // 将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 连接到着色器程序
        GLES20.glLinkProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 计算宽高比
        float ratio = (float) width / height;
        float[] mViewMatrix = new float[16];
        float[] mProjectMatrix = new float[16];

        // 设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix, // 接收相机变换的矩阵
                0,
                0, 0, 7.0f, // 相机位置 x, y , z
                0f, 0f, 0f, // 观测点位置
                0f, 1.0f, 0.0f); // up向量在xyz上的分量
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (needClear) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        }
        // 将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        // 获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        // 指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        // 获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        // 获取片元着色器的fColor成员的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "fColor");
        // 设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, shapePos.length / 3);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void setMatrix(float[] matrix) {
        this.mMVPMatrix = matrix;
    }

    public void setNoNeedClear() {
        this.needClear = false;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

}
