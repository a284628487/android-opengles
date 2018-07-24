/*
 * com.cf.glesdemo.camera.l9.CameraView
 */
package com.ccflying.glescamera.l9;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * Description:
 */
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

	private KitkatCamera mCamera;
	private CameraRender mCameraRender;
	private int cameraId = 1;

	private Runnable mRunnable;

	public CameraView(Context context) {
		this(context, null);
		init();
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
		mCamera = new KitkatCamera();
		mCameraRender = new CameraRender(getResources(), cameraId == 0);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		mCameraRender.onSurfaceCreated(gl, config);
		if (mRunnable != null) {
			mRunnable.run();
			mRunnable = null;
		}
		mCamera.open(cameraId);
		mCameraRender.setCameraId(cameraId);
		Point point = mCamera.getPreviewSize();
		mCameraRender.setDataSize(point.x, point.y);
		mCamera.setPreviewTexture(mCameraRender.getSurfaceTexture());
		mCameraRender.getSurfaceTexture().setOnFrameAvailableListener(
				new SurfaceTexture.OnFrameAvailableListener() {
					@Override
					public void onFrameAvailable(SurfaceTexture surfaceTexture) {
						requestRender();
					}
				});
		mCamera.preview();
	}

	public void switchCamera() {
		mRunnable = new Runnable() {
			@Override
			public void run() {
				mCamera.close();
				cameraId = cameraId == 1 ? 0 : 1;
			}
		};
		onPause();
		onResume();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mCameraRender.setViewSize(width, height);
		GLES20.glViewport(0, 0, width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		mCameraRender.onDrawFrame(gl);
	}

	@Override
	public void onPause() {
		super.onPause();
		mCamera.close();
	}
}
