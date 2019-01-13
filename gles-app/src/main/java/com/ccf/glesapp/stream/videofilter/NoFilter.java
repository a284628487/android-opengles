/*
 *
 * NoFilter.java
 *
 * Created by Wuwang on 2016/11/19
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccf.glesapp.stream.videofilter;

import android.content.res.Resources;

import com.ccf.glesapp.stream.StreamFilter;

/**
 * Description:
 */
public class NoFilter extends StreamFilter {

    public NoFilter(Resources res) {
        super(res);
    }

    @Override
    public void onSurfaceCreated() {
        createProgramByAssetsFile("vshader/video/VideoPreview.shader",
                "fshader/video/VideoPreview.shader");
    }

    @Override
    public void onSizeChanged(int width, int height) {
    }
}
