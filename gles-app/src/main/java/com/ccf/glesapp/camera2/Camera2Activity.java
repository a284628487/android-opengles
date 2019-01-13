package com.ccf.glesapp.camera2;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;

import com.ccf.glesapp.camera2.filter.CameraFilter;
import com.ccf.glesapp.camera2.filter.LightFilter;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Camera2Activity extends AppCompatActivity implements GLSurfaceView.Renderer {

    final String TAG = "Camera2Activity";

    private GLSurfaceView mGLSurfaceView;

    private SurfaceTexture mSurfaceTexture;

    private String cameraId;

    private Surface mSurface;

    private CameraManager cameraManager;

    private CameraDevice cameraDevice;

    private CameraFilter mFilter;

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

        mFilter = new LightFilter(getResources(), true);

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
        if (null != captureSession)
            captureSession.close();
        if (null != cameraDevice)
            cameraDevice.close();
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

    @SuppressLint("MissingPermission")
    private void openCamera() {
        //
        try {
            String[] cids = cameraManager.getCameraIdList();
            for (String cid : cids) {
                CameraCharacteristics stics = cameraManager.getCameraCharacteristics(cid);
                Integer facing = stics.get(CameraCharacteristics.LENS_FACING);
                if (null != facing && facing == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                cameraId = cid;
            }
            //
            updatePreviewSize();
            cameraManager.openCamera(cameraId, cameraOpenCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreviewSize() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MyGLSurfaceView) mGLSurfaceView).setWidthHeight(720, 1280);
            }
        });
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        int textureId = createTextureID();
        mSurfaceTexture = new SurfaceTexture(textureId);
        mSurfaceTexture.setDefaultBufferSize(720, 1280);
        mSurface = new Surface(mSurfaceTexture);
        openCamera();
        // TODO
        mFilter.setTextureId(textureId);
        mFilter.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float[] matrix = new float[16];
        // preview size
        int w = 720;
        int h = 1280;
        //
        Gl2Utils.getShowMatrix(matrix, w, h, width, height);
        mFilter.setMatrix(matrix);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (null != mSurfaceTexture) {
            mSurfaceTexture.updateTexImage();
        }
        // TODO - Filter
        mFilter.onDrawFrame();
    }

    private int createTextureID() {
        int[] texture = new int[1];
        // 生成纹理
        GLES20.glGenTextures(1, texture, 0);
        // 绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        //
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        //
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        //
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        //
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        //
        return texture[0];
        //
        // 由于我们创建的是扩展纹理，所以绑定的时候我们也需要绑定到扩展纹理上才可以正常使用，
        // GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,texture[0])。
    }
}
