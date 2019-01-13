/*
 *
 * GrayFilter.java
 *
 * Created by Wuwang on 2016/12/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccf.glesapp.video.filter;

import android.content.res.Resources;

/**
 * Description:
 */
public class GrayFilter extends CameraFilter {

    public GrayFilter(Resources mRes, boolean back) {
        super(mRes, back);
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/video/VideoPreviewGray.shader",
                "fshader/video/VideoPreviewGray.shader");
    }

    @Override
    public void onSizeChanged(int width, int height) {
    }
}
