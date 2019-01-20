package com.ccf.glesapp.stream.camerafilter;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.ccf.glesapp.stream.StreamFilter;
import com.ccf.glesapp.util.Gl2Utils;

import java.util.Random;

public class CenterFlashFilter extends StreamFilter {

    public CenterFlashFilter(Resources mRes, boolean back) {
        super(mRes, back);
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/camera/CameraPreviewFlashCenter.shader",
                "fshader/camera/CameraPreviewFlashCenter.shader");
    }

    private void saveOriginalMatrixIfNeeded() {
    }

    @Override
    public void onSizeChanged(int width, int height) {
        saveOriginalMatrixIfNeeded();
    }

}
