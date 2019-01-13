package com.ccf.glesapp.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

import java.io.IOException;

@SuppressLint("NewApi")
public class CameraTexActivity extends Activity {
	final String TAG = "CameraActivity";
	private TextureView mTextureView;
	private SurfaceTexture mSurfaceTexture;
	private Camera mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTextureView = new TextureView(this);
		setContentView(mTextureView);
		mTextureView.setSurfaceTextureListener(new SurfaceTextureListener() {

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture arg0,
					int arg1, int arg2) {
			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
				stopCamera();
				return true;
			}

			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture arg0,
					int arg1, int arg2) {
				mSurfaceTexture = arg0;
				openCameraAndPreview(mSurfaceTexture, arg1, arg2);
			}
		});
	}

	private void openCameraAndPreview(final SurfaceTexture surface,
			final int width, final int height) {
		mCamera = Camera.open();
		mCamera.setDisplayOrientation(90);
		try {
			mCamera.setPreviewTexture(surface);
			mCamera.startPreview();
			mCamera.setPreviewCallback(new PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					// camera.addCallbackBuffer(data);
				}
			});
		} catch (IOException ioe) {
			// Something bad happened
		}
	}

	private void stopCamera() {
		if (null == mCamera) {
			return;
		}
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();
		mCamera.release();
	}
}
