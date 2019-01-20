package com.ccf.glesapp.stream.camerafilter;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.ccf.glesapp.stream.StreamFilter;
import com.ccf.glesapp.util.Gl2Utils;

import java.util.Random;

public class FlashFilter extends StreamFilter {

    private int mFlashScaleHandle;

    private Random mRandom;

    private float[] mBaseMatrix;

    private int index = 0;

    private float[] mSizeScale = new float[]{
            1.0f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f,
            1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1.0f
    };

    public FlashFilter(Resources mRes, boolean back) {
        super(mRes, back);
        mRandom = new Random();
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/camera/CameraPreviewFlash.shader",
                "fshader/camera/CameraPreviewFlash.shader");
        //
        mFlashScaleHandle = GLES20.glGetUniformLocation(mProgram, "flashScale");
    }

    private void saveOriginalMatrixIfNeeded() {
        if (null == mBaseMatrix) {
            float[] ori = getMatrix();
            mBaseMatrix = new float[16];
            System.arraycopy(ori, 0, mBaseMatrix, 0, 16);
        }
    }

    @Override
    public void onSizeChanged(int width, int height) {
        saveOriginalMatrixIfNeeded();
    }

    @Override
    protected void onSetExpandData() {
        saveOriginalMatrixIfNeeded();
        float[] matrix = new float[16];
        System.arraycopy(mBaseMatrix, 0, matrix, 0, 16);
        float sizeScale = mSizeScale[(index++) % 12];
        Gl2Utils.scale(matrix, sizeScale, sizeScale);
        setMatrix(matrix);
        //
        super.onSetExpandData();
    }

    @Override
    public void onDraw() {
        super.onDraw();
        float f = mRandom.nextFloat();
        boolean tj = mRandom.nextInt() % 2 == 0;
        float flashScale = 1;
        if (tj) {
            flashScale += f * 1.2f;
        } else {
            flashScale -= f * 1.2f;
        }
        if (flashScale < 0.3f) {
            flashScale = 0.3f;
        }
        GLES20.glUniform1f(mFlashScaleHandle, flashScale);
    }
}
