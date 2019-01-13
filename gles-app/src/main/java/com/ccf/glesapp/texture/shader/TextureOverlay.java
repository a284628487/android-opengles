package com.ccf.glesapp.texture.shader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.ccf.glesapp.R;
import com.ccf.glesapp.util.Utils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by ccfyyn on 18/7/1.
 */
public class TextureOverlay implements GLSurfaceView.Renderer {

    final String TAG = "TextureOverlay";
    private final Context mContext;

    private String vShaderFileName = "vshader/" + TAG + ".shader";
    private String fShaderFileName = "fshader/" + TAG + ".shader";

    private Bitmap mBitmap, mBitmapOverlay;

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

    public TextureOverlay(Context context) {
        this.mContext = context;
        //
        try {
            mBitmap = BitmapFactory.decodeStream(mContext.getAssets().open("mm.png"));
            mBitmapOverlay = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_qrcode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mGLPosition = Utils.allocateFloatBuffer(vertexCoords);
        mTexture1Coord = Utils.allocateFloatBuffer(textureCoords1);
        mOverlayCoord = Utils.allocateFloatBuffer(textureCoords2);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_TEXTURE_2D | GLES20.GL_DEPTH_TEST);
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        //
        mProgram = Utils.createProgramFromAssets(mContext.getResources(),
                vShaderFileName, fShaderFileName);
        //
        textureId0 = Utils.createTexture(mBitmap);
        textureId1 = Utils.createTexture(mBitmapOverlay);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float[] mProjectMatrix = new float[16];
        float[] mViewMatrix = new float[16];

        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        //
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        // Matrix.orthoM(m, mOffset, left, right, bottom, top, near, far);
        // 值小的一边保持值为1

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
}

// https://blog.csdn.net/keen_zuxwang/article/details/78362058?locationNum=3&fps=1
// https://blog.csdn.net/prahs/article/details/49818345
