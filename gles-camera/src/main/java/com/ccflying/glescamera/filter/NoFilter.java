/*
 *
 * NoFilter.java
 * 
 * Created by Wuwang on 2016/11/19
 * Copyright © 2016年 深圳哎吖科技. All rights reserved.
 */
package com.ccflying.glescamera.filter;

import android.content.res.Resources;

/**
 * Description:
 */
public class NoFilter extends CameraFilter {

	public NoFilter(Resources res, boolean back) {
		super(res, back);
	}

	@Override
	public void onSurfaceCreated() {
		createProgramByAssetsFile("vshader/CameraPreview.shader",
				"fshader/CameraPreview.shader");
	}

	@Override
	public void onSizeChanged(int width, int height) {
	}
}
