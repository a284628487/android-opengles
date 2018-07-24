package com.ccflying.glestexture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.ccflying.glestexture.base.BaseRenderer;
import com.ccflying.glestexture.util.ShaderUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ccfyyn on 18/7/1.
 */

public class TextureOverlay extends BaseRenderer {

    final String TAG = "TextureOverlay";

    private String vShaderFileName, fShaderFileName;

    private Bitmap mBitmap1, mBitmap2;

    private final float[] vertexCoords = {-1.0f, 1.0f, // 左上角
            -1.0f, -1.0f, // 左下角
            1.0f, 1.0f, // 右上角
            1.0f, -1.0f // 右下角
    };

    private final float[] textureCoords1 = {0.0f, 0.0f, // 左上
            0.0f, 1.0f,// 左下
            1.0f, 0.0f,// 右上
            1.0f, 1.0f,// 右下
    };

    private final float[] textureCoords2 = {0.0f, 0.0f, // 左上
            0.0f, 1.0f,// 左下
            1.0f, 0.0f,// 右上
            1.0f, 1.0f,// 右下
    };

    private FloatBuffer mGLPosition;
    private FloatBuffer mTexture1Coord, mOverlayCoord;

    private int mProgram;

    private float[] mMVPMatrix = new float[16];

    private int textureId0;
    private int textureId1;

    public TextureOverlay(GLSurfaceView view) {
        super(view);
        vShaderFileName = "vshader/" + TAG + ".shader";
        fShaderFileName = "fshader/" + TAG + ".shader";
        //
        try {
            mBitmap1 = BitmapFactory.decodeStream(view.getResources()
                    .getAssets().open("mm.png"));
            mBitmap2 = BitmapFactory.decodeResource(view.getResources(), R.drawable.ic_qrcode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBitmap(Bitmap b1, Bitmap b2) {
        this.mBitmap1 = b1;
        this.mBitmap2 = b2;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_TEXTURE_2D | GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        mGLPosition = allocateFloatBuffer(vertexCoords);
        mTexture1Coord = allocateFloatBuffer(textureCoords1);
        mOverlayCoord = allocateFloatBuffer(textureCoords2);
        //
        mProgram = ShaderUtils.createProgram(mView.getResources(),
                vShaderFileName, fShaderFileName);
        //
        textureId0 = createTexture(mBitmap1);
        textureId1 = createTexture(mBitmap2);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float[] mProjectMatrix = new float[16];
        float[] mViewMatrix = new float[16];

        int w = mBitmap1.getWidth();
        int h = mBitmap1.getHeight();
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
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);

        int vMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        int vertexPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int mBaseCoordHandle = GLES20.glGetAttribLocation(mProgram, "vCoord");
        int mOverlayCoordHandle = GLES20.glGetAttribLocation(mProgram, "vCoord2");

        int mTextureHandle = GLES20.glGetUniformLocation(mProgram, "vTexture");
        int mTexture2Handle = GLES20.glGetUniformLocation(mProgram, "vTexture2");

        GLES20.glUniformMatrix4fv(vMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(vertexPositionHandle);
        GLES20.glEnableVertexAttribArray(mBaseCoordHandle);
        GLES20.glEnableVertexAttribArray(mOverlayCoordHandle);

        // 传入顶点坐标
        GLES20.glVertexAttribPointer(vertexPositionHandle, 2, GLES20.GL_FLOAT,
                false, 0, mGLPosition);
        // 传入纹理1坐标
        GLES20.glVertexAttribPointer(mBaseCoordHandle, 2, GLES20.GL_FLOAT,
                false, 0, mTexture1Coord);
        // 传入纹理2坐标
        GLES20.glVertexAttribPointer(mOverlayCoordHandle, 2, GLES20.GL_FLOAT,
                false, 0, mOverlayCoord);
        // 绑定纹理1
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId0);
        GLES20.glUniform1i(mTextureHandle, 0);
        // 绑定纹理2
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId1);
        GLES20.glUniform1i(mTexture2Handle, 1);
        //
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        //
        GLES20.glDisableVertexAttribArray(vertexPositionHandle);
        GLES20.glDisableVertexAttribArray(mBaseCoordHandle);
        GLES20.glDisableVertexAttribArray(mOverlayCoordHandle);
    }

    private int createTexture(Bitmap bmp) {
        int[] texture = new int[1];
        if (bmp != null && !bmp.isRecycled()) {
            int target1 = GLES20.GL_TEXTURE_2D;
            // 生成纹理(GLsizei n, textures, offset)
            GLES20.glGenTextures(1, texture, 0); // 生成一个纹理
            // 生成纹理(target, texture)
            GLES20.glBindTexture(target1, texture[0]);
            // 设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(target1,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            // 设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(target1,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            // 设置环绕方向S，截取纹理坐标到[1/2n, 1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(target1,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            // 设置环绕方向T，截取纹理坐标到[1/2n, 1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(target1,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            // 根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(target1, 0, bmp, 0);
            //
        }
        Log.e(TAG, "createTexture: " + texture[0]);
        return texture[0];
    }
}

// https://blog.csdn.net/keen_zuxwang/article/details/78362058?locationNum=3&fps=1
// https://blog.csdn.net/prahs/article/details/49818345
