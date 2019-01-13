/*
 *
 * GrayFilter.java
 *
 * Created by Wuwang on 2016/12/14
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccf.glesapp.stream.videofilter;

import android.content.res.Resources;

import com.ccf.glesapp.stream.StreamFilter;

/**
 * Description:
 */
public class GrayFilter extends StreamFilter {

    public GrayFilter(Resources mRes) {
        super(mRes);
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
