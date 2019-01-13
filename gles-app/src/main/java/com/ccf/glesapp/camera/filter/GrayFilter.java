/*
 *
 * GrayFilter.java
 *
 * Created by Wuwang on 2016/12/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccf.glesapp.camera.filter;

import android.content.res.Resources;

/**
 * Description:
 */
public class GrayFilter extends CameraFilter {

    public GrayFilter(Resources mRes, boolean back) {
        super(mRes, back);
        needMatrixOperation = true;
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/camera/CameraPreviewGray.shader",
                "fshader/camera/CameraPreviewGray.shader");
    }

    @Override
    public void onSizeChanged(int width, int height) {
    }
}
