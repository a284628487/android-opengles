package com.ccf.glesapp.camera2;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageWriter;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;

import com.ccf.glesapp.R;
import com.ccf.glesapp.stream.camerafilter.CenterFlashFilter;
import com.ccf.glesapp.stream.camerafilter.FlashFilter;
import com.ccf.glesapp.stream.camerafilter.GrayFilter;
import com.ccf.glesapp.stream.camerafilter.LightFilter;
import com.ccf.glesapp.stream.StreamFilter;
import com.ccf.glesapp.stream.camerafilter.NoFilter;
import com.ccf.glesapp.util.Gl2Utils;
import com.ccf.glesapp.util.Utils;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Camera2Activity extends AppCompatActivity implements GLSurfaceView.Renderer {

    final String TAG = "Camera2Activity";

    private MyGLSurfaceView mGLSurfaceView;

    private SurfaceTexture mSurfaceTexture;

    private String cameraId;

    private Surface mSurface;

    private CameraManager cameraManager;

    private CameraDevice cameraDevice;

    private StreamFilter mFilter;

    private Size mPreviewSize;

    private int surfaceWidth = 0;
    private int surfaceHeight = 0;

    private final int PREFERED_WIDTH = 1280;
    private final int PREFERED_HEIGHT = 720;

    private boolean mIsBackCamera = true;

    private int textureId = 0;

    private boolean mNeedChangeFilter = false;
    private boolean mNeedChangeCamera = false;
    private Runnable mChangeFilterRunnable = null;

    private CameraDevice.StateCallback cameraOpenCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
        }
    };
    private HandlerThread mBackTherad;
    private Handler mCameraHandler;
    private CaptureRequest.Builder builder;
    private CameraCaptureSession captureSession;
    private CameraCaptureSession.StateCallback captureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            captureSession = session;
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new MyGLSurfaceView(this);
        setContentView(mGLSurfaceView);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mBackTherad = new HandlerThread("wtf");
        mBackTherad.start();
        mCameraHandler = new Handler(mBackTherad.getLooper());

        mFilter = new LightFilter(getResources(), mIsBackCamera);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
        //
        releasePreview();
    }

    private void releasePreview() {
        if (null != captureSession) {
            captureSession.close();
        }
        if (null != cameraDevice) {
            cameraDevice.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBackTherad.quitSafely();
    }

    private void updatePreview() {
        try {
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureSession.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    mGLSurfaceView.requestRender();
                }

                @Override
                public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session,
                                                       int sequenceId, long frameNumber) {
                    super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
                }
            }, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        try {
            builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mSurface);
            cameraDevice.createCaptureSession(Arrays.asList(mSurface), captureSessionCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size choosePreviewSize() {
        //
        Size previewSize = null;
        try {
            String[] cids = cameraManager.getCameraIdList();
            for (String cid : cids) {
                CameraCharacteristics stics = cameraManager.getCameraCharacteristics(cid);
                Integer facing = stics.get(CameraCharacteristics.LENS_FACING);
                int excludeCameraId = (mIsBackCamera ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK);
                if (null != facing && facing == excludeCameraId)
                    continue;
                cameraId = cid;
                stics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
                // 获得流配置
                StreamConfigurationMap map = stics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                // 获取摄像头支持的所有尺寸
                Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
                for (int i = 0; i < sizes.length; i++) {
                    Log.d(TAG, "choosePreviewSize: " + sizes[i].getWidth() + ", " + sizes[i].getHeight());
                    if (sizes[i].getWidth() == PREFERED_WIDTH && sizes[i].getHeight() == PREFERED_HEIGHT) {
                        previewSize = sizes[i];
                        break;
                    }
                }
            }
            if (null == previewSize) {
                previewSize = new Size(PREFERED_WIDTH, PREFERED_HEIGHT);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return previewSize;
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            cameraManager.openCamera(cameraId, cameraOpenCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreviewSize(final int previewWidth, final int previewHeight) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGLSurfaceView.setWidthHeight(previewWidth, previewHeight);
            }
        });
    }

    private void switchCamera() {
        Log.e(TAG, "switchCamera: thread = " + Thread.currentThread().getName());
        // releasePreview
        releasePreview();
        mIsBackCamera = !mIsBackCamera;
        // 获取previewSize
        mPreviewSize = choosePreviewSize();
        // 打开Camera
        openCamera();
        //
        mNeedChangeCamera = true;
    }

    private void changeGrayFilter() {
        mNeedChangeFilter = true;
        mChangeFilterRunnable = new Runnable() {
            @Override
            public void run() {
                mFilter = new GrayFilter(getResources(), mIsBackCamera);
                mFilter.setTextureId(textureId);
                mFilter.onSurfaceCreated();
            }
        };
    }

    private void changeNoFilter() {
        mNeedChangeFilter = true;
        mChangeFilterRunnable = new Runnable() {
            @Override
            public void run() {
                mFilter = new NoFilter(getResources(), mIsBackCamera);
                mFilter.setTextureId(textureId);
                mFilter.onSurfaceCreated();
            }
        };
    }

    private void changeLightFilter() {
        mNeedChangeFilter = true;
        mChangeFilterRunnable = new Runnable() {
            @Override
            public void run() {
                mFilter = new LightFilter(getResources(), mIsBackCamera);
                mFilter.setTextureId(textureId);
                mFilter.onSurfaceCreated();
            }
        };
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(TAG, "onSurfaceCreated: thread = " + Thread.currentThread().getName());
        // 获取previewSize
        mPreviewSize = choosePreviewSize();
        // 摄像机直接获取到的数据是旋转90度的，所以需要对调宽高。
        updatePreviewSize(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        // 创建Texture
        textureId = Utils.createStreamTexture();
        mSurfaceTexture = new SurfaceTexture(textureId);
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mSurface = new Surface(mSurfaceTexture);
        // TODO
        mFilter.setTextureId(textureId);
        mFilter.onSurfaceCreated();
        // 打开Camera
        openCamera();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.e(TAG, "onSurfaceChanged: thread = " + Thread.currentThread().getName());
        surfaceWidth = width;
        surfaceHeight = height;
        //
        GLES20.glViewport(0, 0, width, height);
        float[] matrix = new float[16];
        // preview size
        int imageWidth = mPreviewSize.getHeight();
        int imageHeight = mPreviewSize.getWidth();
        //
        Gl2Utils.getShowMatrix(matrix, imageWidth, imageHeight, width, height);
        mFilter.setMatrix(matrix);
        mFilter.onSizeChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (null != mSurfaceTexture) {
            mSurfaceTexture.updateTexImage();
        }
        // TODO - Filter
        mFilter.onDrawFrame();
        //
        if (mNeedChangeFilter) {
            if (null != mChangeFilterRunnable) {
                mChangeFilterRunnable.run();
                mNeedChangeFilter = false;
            }
        } else if (mNeedChangeCamera) {
            // 改变摄像头
            mFilter.switchCamera();
            mNeedChangeCamera = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera2_menus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switchCamera:
                switchCamera();
                break;
            case R.id.lightFilter:
                changeLightFilter();
                break;
            case R.id.grayFilter:
                changeGrayFilter();
                break;
            case R.id.noFilter:
                changeNoFilter();
                break;
            case R.id.flashFilter:
                changeFlashFilter();
                break;
            case R.id.flashAtFilter:
                changeFlashCenterFilter();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeFlashFilter() {
        mNeedChangeFilter = true;
        mChangeFilterRunnable = new Runnable() {
            @Override
            public void run() {
                mFilter = new FlashFilter(getResources(), mIsBackCamera);
                mFilter.setTextureId(textureId);
                mFilter.onSurfaceCreated();
            }
        };
    }


    private void changeFlashCenterFilter() {
        mNeedChangeFilter = true;
        mChangeFilterRunnable = new Runnable() {
            @Override
            public void run() {
                mFilter = new CenterFlashFilter(getResources(), mIsBackCamera);
                mFilter.setTextureId(textureId);
                mFilter.onSurfaceCreated();
            }
        };
    }
}
