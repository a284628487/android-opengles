package com.ccf.glesapp.texture.shader;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.ccf.glesapp.util.Utils;

// https://blog.csdn.net/junzia/article/details/52842816
public class TextureShape implements GLSurfaceView.Renderer {

    public static final int COORDS_PER_VERTEX = 3;

    final String TAG = "TextureShape";

    protected String vShaderFileName = "vshader/TextureShape.shader";

    protected String fShaderFileName = "fshader/TextureShape.shader";

    protected Bitmap mBitmap;
    protected int textureId;

    protected FloatBuffer textureBuffer;
    protected FloatBuffer vertexBuffer;

    protected int mProgram;

    // 变换矩阵
    protected float[] mMVPMatrix = new float[16];

    protected final float[] vertexCoords = { //
            -1.0f, 1.0f, // 左上角
            -1.0f, -1.0f, // 左下角
            1.0f, 1.0f, // 右上角
            1.0f, -1.0f // 右下角
    };

    protected final float[] textureCoords = { //
            0.0f, 0.0f, // 左上
            0.0f, 1.0f,// 左下
            1.0f, 0.0f,// 右上
            1.0f, 1.0f,// 右下
    };

    // 纹理坐标句柄
    protected int texturePositionHandle;
    protected int textureHandle;
    // 顶点坐标句柄
    protected int vertexPositionHandle;
    protected int vMatrixHandle;

    protected Context mContext;

    public TextureShape(Context context) {
        this.mContext = context;
        //
        textureBuffer = Utils.allocateFloatBuffer(textureCoords);
        vertexBuffer = Utils.allocateFloatBuffer(vertexCoords);
    }

    public TextureShape(Context context, String vShader, String fShader) {
        this.mContext = context;
        this.vShaderFileName = vShader;
        this.fShaderFileName = fShader;
        //
        textureBuffer = Utils.allocateFloatBuffer(textureCoords);
        vertexBuffer = Utils.allocateFloatBuffer(vertexCoords);
    }

    public void setBitmap(Bitmap bmp) {
        this.mBitmap = bmp;
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        vMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        texturePositionHandle = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        vertexPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 纹理句柄
        textureHandle = GLES20.glGetUniformLocation(mProgram, "vTexture");

        onProgramDrawFrame();

        GLES20.glUniformMatrix4fv(vMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(vertexPositionHandle);
        GLES20.glEnableVertexAttribArray(texturePositionHandle);
        GLES20.glUniform1i(textureHandle, 0);
        textureId = createTexture();
        // 传入顶点坐标
        GLES20.glVertexAttribPointer(vertexPositionHandle, 2, GLES20.GL_FLOAT,
                false, 0, vertexBuffer);
        // 传入纹理坐标
        GLES20.glVertexAttribPointer(texturePositionHandle, 2, GLES20.GL_FLOAT,
                false, 0, textureBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //
        GLES20.glDisableVertexAttribArray(vertexPositionHandle);
        GLES20.glDisableVertexAttribArray(texturePositionHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float[] mViewMatrix = new float[16];
        float[] mProjectMatrix = new float[16];

        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        //
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        // uXY = sWidthHeight;
        // Matrix.orthoM(m, mOffset, left, right, bottom, top, near, far);
        // 长的一方保持为坐标刻度值1 ??? -> No

        // 屏幕宽大于屏幕高
        if (width > height) { // -> sWidthHeight 大于 1
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH,
                        sWidthHeight * sWH, -1, 1, 3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH,
                        sWidthHeight / sWH, -1, 1, 3, 5);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1,
                        -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight,
                        sWH / sWidthHeight, 3, 5);
            }
        }
        // 设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, 5.0f,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f);
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        GLES20.glEnable(GLES20.GL_TEXTURE_2D | GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        //
        mProgram = Utils.createProgramFromAssets(mContext.getResources(), vShaderFileName, fShaderFileName);

        onProgramCreated(mProgram);
    }

    protected int createTexture() {
        int[] texture = new int[1];
        if (mBitmap != null && !mBitmap.isRecycled()) {
            // 生成纹理(GLsizei n, textures, offset)
            GLES20.glGenTextures(1, texture, 0);
            // 生成纹理(target, texture)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            // 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            // 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            // 设置环绕方向S，截取纹理坐标到[1/2n, 1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            // 设置环绕方向T，截取纹理坐标到[1/2n, 1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            // 根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
            return texture[0];
        }
        return 0;
    }

    protected void onProgramDrawFrame() {

    }

    protected void onProgramCreated(int mProgram) {

    }

}
