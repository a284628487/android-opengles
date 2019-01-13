/*
 *
 * NoFilter.java
 *
 * Created by Wuwang on 2016/11/19
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccf.glesapp.stream.camerafilter;

import android.content.res.Resources;

import com.ccf.glesapp.stream.StreamFilter;

/**
 * Description:
 */
public class NoFilter extends StreamFilter {

    public NoFilter(Resources res, boolean back) {
        super(res, back);
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/camera/CameraPreview.shader",
                "fshader/camera/CameraPreview.shader");
    }

    @Override
    public void onSizeChanged(int width, int height) {
    }
}
