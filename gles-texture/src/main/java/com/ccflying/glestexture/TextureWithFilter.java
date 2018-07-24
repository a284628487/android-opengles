package com.ccflying.glestexture;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.ccflying.glestexture.base.TextureFilter;

import javax.microedition.khronos.opengles.GL10;

public class TextureWithFilter extends TextureShape {

    private TextureFilter mFilter = TextureFilter.NONE;
    private int changeTypeHandle;
    private int changeColorHandle;
    private int uXYHandle;
    private float uXY = 0;

    public void setFilter(TextureFilter f) {
        this.mFilter = f;
    }

    public TextureWithFilter(GLSurfaceView view) {
        super(view, "vshader/TextureWithFilter.shader", "fshader/TextureWithFilter.shader");
    }

    @Override
    protected void onProgramDrawFrame() {
        super.onProgramDrawFrame();
        Log.d(TAG, "onProgramDrawFrame#");
        GLES20.glUniform1i(changeTypeHandle, mFilter.getType());
        GLES20.glUniform3fv(changeColorHandle, 1, mFilter.data(), 0);
        GLES20.glUniform1fv(uXYHandle, 1, new float[]{uXY}, 0);
    }

    @Override
    protected void onProgramCreated(int mProgram) {
        super.onProgramCreated(mProgram);
        Log.d(TAG, "onProgramCreated#");
        changeTypeHandle = GLES20.glGetUniformLocation(mProgram, "vChangeType");
        changeColorHandle = GLES20.glGetUniformLocation(mProgram, "vChangeColor");
        uXYHandle = GLES20.glGetUniformLocation(mProgram, "uXY");
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        super.onSurfaceChanged(arg0, width, height);
        if (null == mBitmap) {
            uXY = width * 1.0f / height;
        } else {
            uXY = mBitmap.getWidth() * 1.0f / mBitmap.getHeight();
        }
    }
}
