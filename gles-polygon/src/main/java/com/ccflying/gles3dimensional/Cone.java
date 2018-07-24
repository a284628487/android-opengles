package com.ccflying.gles3dimensional;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.ccflying.util.Utils;

/**
 * Description:圆锥
 */
public class Cone implements GLSurfaceView.Renderer {

    private final int COORDS_PER_VERTEX = 3;

    private final static String TAG = "Cone";

    private int mProgram;

    private String vShader, fShader;

    private Oval oval;
    private FloatBuffer vertexBuffer;

    private float[] mMVPMatrix = new float[16];

    private int n = 360; // 切割份数
    private float height = 2.0f; // 圆锥高度
    private float radius = 1.0f; // 圆锥底面半径
    private float[] shapePositions;
    // 顶点个数
    private int vertexSize;

    public Cone(Context context) {

        vShader = Utils.getAssetsResourceString(context.getResources(),
                "vshader/" + TAG + ".shader");
        fShader = Utils.getAssetsResourceString(context.getResources(),
                "fshader/" + TAG + ".shader");

        oval = new Oval(context, 0, 1);
        oval.setNoNeedClear();

        ArrayList<Float> pos = new ArrayList<>();
        // 顶点。
        pos.add(0.0f);
        pos.add(0.0f);
        pos.add(height);
        float angDegSpan = 360f / n;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            pos.add((float) (radius * Math.sin(i * Math.PI / 180f)));
            pos.add((float) (radius * Math.cos(i * Math.PI / 180f)));
            pos.add(0.0f);
        }
        shapePositions = new float[pos.size()];
        for (int i = 0; i < shapePositions.length; i++) {
            shapePositions[i] = pos.get(i);
        }
        vertexSize = shapePositions.length / 3;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        vertexBuffer = Utils.allocateFloatBuffer(shapePositions);

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

        oval.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        // 计算宽高比
        float ratio = (float) width / height;
        float[] mViewMatrix = new float[16];
        float[] mProjectMatrix = new float[16];

        // 设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0,
                10.0f, -10.0f, -4.0f,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f);
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        int mMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(mMatrix, 1, false, mMVPMatrix, 0);
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexSize);
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        oval.setMatrix(mMVPMatrix);
        oval.onDrawFrame(gl);
    }
}
