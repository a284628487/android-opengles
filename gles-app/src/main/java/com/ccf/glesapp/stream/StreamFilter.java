package com.ccf.glesapp.stream;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.ccf.glesapp.util.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * Description:
 */
public abstract class StreamFilter {

    private static final String TAG = "StreamFilter";

    protected boolean forVideo = false;

    public static boolean DEBUG = true;
    /**
     * 单位矩阵
     */
    public static final float[] OM = {
            1, 0, 0, 0, //
            0, 1, 0, 0, //
            0, 0, 1, 0, //
            0, 0, 0, 1 //
    };
    /**
     * 程序句柄
     */
    protected int mProgram;
    /**
     * 顶点坐标句柄
     */
    protected int mHPosition;
    /**
     * 纹理坐标句柄
     */
    protected int mHCoord;
    /**
     * 总变换矩阵句柄
     */
    protected int mHMatrix;
    /**
     * 默认纹理贴图句柄
     */
    protected int mHTexture;

    protected Resources mRes;

    /**
     * 顶点坐标Buffer
     */
    protected FloatBuffer mVerBuffer;

    /**
     * 纹理坐标Buffer
     */
    protected FloatBuffer mTexBuffer;

    /**
     * 索引坐标Buffer
     */
    protected ShortBuffer mIndexBuffer;

    protected int mFlag = 0;

    private float[] matrix = Arrays.copyOf(OM, 16);

    private int textureType = 0; // 默认使用Texture2D
    protected int textureId = 0;
    // 顶点坐标
    protected float coordXY[] = {//
            -1.0f, 1.0f, // 左上
            -1.0f, -1.0f, // 左下
            1.0f, 1.0f, // 右上
            1.0f, -1.0f, // 右下
    };

    // 纹理坐标-原始坐标
    protected float[] coordOriginal = { //
            0.0f, 0.0f, // 左上
            0.0f, 1.0f, // 左下
            1.0f, 0.0f, // 右上
            1.0f, 1.0f, // 右下
    };

    // 纹理坐标-后置摄像头
    protected float[] coordBack = { //
            0.0f, 1.0f, // 左上
            1.0f, 1.0f, // 左下
            0.0f, 0.0f, // 右上
            1.0f, 0.0f, // 右下
    };
    // 整体顺时针旋转90度。

    // 纹理坐标-前置摄像头
    protected float[] coordFront = { //
            1.0f, 1.0f, // 左上
            0.0f, 1.0f, // 左下
            1.0f, 0.0f, // 右上
            0.0f, 0.0f, // 右下
    };
    // 沿着(0,1)->(1,0)轴做镜像翻转。

    private boolean isBackCamera;

    public StreamFilter(Resources mRes, boolean back) {
        this.mRes = mRes;
        this.isBackCamera = back;
        initBuffer();
    }

    public StreamFilter(Resources mRes) {
        this.mRes = mRes;
        this.forVideo = true;
        initBuffer();
    }

    public final void setSize(int width, int height) {
        onSizeChanged(width, height);
    }

    public void onDrawFrame() {
        onClear();
        onUseProgram();
        onSetExpandData();
        onBindTexture();
        onDraw();
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    public float[] getMatrix() {
        return matrix;
    }

    public final void setTextureType(int type) {
        this.textureType = type;
    }

    public final int getTextureType() {
        return textureType;
    }

    public final int getTextureId() {
        return textureId;
    }

    public final void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public int getOutputTexture() {
        return -1;
    }

    /**
     * 实现此方法，完成程序的创建，可直接调用createProgram来实现
     */
    public abstract void onSurfaceCreated();

    public abstract void onSizeChanged(int width, int height);

    protected final void createProgram(String vertex, String fragment) {
        mProgram = Utils.createProgram(vertex, fragment);
        mHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mHCoord = GLES20.glGetAttribLocation(mProgram, "vCoord");
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        mHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
    }

    protected final void createProgramByAssetsFile(String vertexFile, String fragmentFile) {
        createProgram(Utils.getAssetsResourceString(mRes, vertexFile),
                Utils.getAssetsResourceString(mRes, fragmentFile));
    }

    /**
     * Buffer初始化
     */
    protected void initBuffer() {
        mVerBuffer = Utils.allocateFloatBuffer(coordXY);
        if (forVideo) {
            mTexBuffer = Utils.allocateFloatBuffer(coordOriginal);
        } else {
            if (isBackCamera) {
                mTexBuffer = Utils.allocateFloatBuffer(coordBack);
            } else {
                mTexBuffer = Utils.allocateFloatBuffer(coordFront);
            }
        }
    }

    protected void onUseProgram() {
        GLES20.glUseProgram(mProgram);
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected void onDraw() {
        GLES20.glEnableVertexAttribArray(mHPosition);
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0,
                mVerBuffer);
        GLES20.glEnableVertexAttribArray(mHCoord);
        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0,
                mTexBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mHPosition);
        GLES20.glDisableVertexAttribArray(mHCoord);
    }

    /**
     * 清除画布
     */
    protected void onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * 设置其他扩展数据
     */
    protected void onSetExpandData() {
        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, getMatrix(), 0);
    }

    /**
     * 绑定默认纹理
     */
    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());
        GLES20.glUniform1i(mHTexture, textureType);
    }

    public void switchCamera() {
        isBackCamera = !isBackCamera;
        initBuffer();
    }
}
