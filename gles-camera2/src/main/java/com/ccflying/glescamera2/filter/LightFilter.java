/*
 *
 * GrayFilter.java
 *
 * Created by Wuwang on 2016/12/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccflying.glescamera2.filter;

import android.content.res.Resources;

/**
 * Description:
 */
public class LightFilter extends CameraFilter {

    public LightFilter(Resources mRes, boolean back) {
        super(mRes, back);
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/CameraPreviewLight.shader",
                "fshader/CameraPreviewLight.shader");
    }

    @Override
    public void onSizeChanged(int width, int height) {
    }
}
