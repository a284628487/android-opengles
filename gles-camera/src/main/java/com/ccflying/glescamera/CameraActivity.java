package com.ccflying.glescamera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.ccflying.glescamera.filter.CameraFilter;
import com.ccflying.glescamera.filter.GrayFilter;
import com.ccflying.glescamera.util.Gl2Utils;

@SuppressLint("NewApi")
public class CameraActivity extends Activity implements Renderer {
    private float[] matrix = new float[16];
    final String TAG = "CameraActivity";
    private GLSurfaceView mSurfaceView;
    private SurfaceTexture mSurfaceTexture;
    private Camera mCamera;
    private CameraFilter mFilter;

    private int textureId = 0;
    private int cameraId = 0;

    private int viewWidth, viewHeight;

    private LinkedList<Runnable> mRunAfterList = new LinkedList<Runnable>();

    private void calculateMatrix() {
        matrix = new float[16];
        Gl2Utils.getShowMatrix(matrix, viewWidth, viewHeight, viewWidth,
                viewHeight);
        // 计算坐标信息
        if (cameraId == 1) {
            Gl2Utils.flip(matrix, true, false);
            Gl2Utils.rotate(matrix, 180);
        } else {
            // Gl2Utils.rotate(matrix, 45);
        }
        mFilter.setMatrix(matrix);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mSurfaceView = findViewById(R.id.glSurfaceView);
        mSurfaceView.setEGLContextClientVersion(2);
        mSurfaceView.setRenderer(this);
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        // mFilter = new NoFilter(getResources(), cameraId == 0);
        cameraId = getIntent().getIntExtra("cid", 0);
        mFilter = new GrayFilter(getResources(), cameraId == 0);
        // mFilter = new OverlayFilter(getResources());
    }

    private void openCameraAndPreview(final SurfaceTexture surface,
                                      final int width, final int height) {
        mCamera = Camera.open(cameraId);
        try {
            OnFrameAvailableListener listener = new OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture arg0) {
                    if (null == mCamera)
                        return;
                    mSurfaceView.requestRender();
                }
            };
            // 给 SurfaceTexture 添加 FrameAvailable 的监听
            mSurfaceTexture.setOnFrameAvailableListener(listener);
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.startPreview();
        } catch (IOException ioe) {
            // Something Bad Happened
        }
    }

    private void stopCamera() {
        if (null == mCamera) {
            return;
        }
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.viewHeight = width;
        this.viewWidth = height;
        calculateMatrix();
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        if (null != mSurfaceTexture) {
            mSurfaceTexture.updateTexImage();
        }
        // TODO - Filter
        mFilter.onDrawFrame();
        // TODO - After
        for (int i = 0; i < mRunAfterList.size(); i++) {
            Runnable runnable = mRunAfterList.get(i);
            runnable.run();
        }
        mRunAfterList.clear();
    }

    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        textureId = createTextureID();
        mSurfaceTexture = new SurfaceTexture(textureId);
        openCameraAndPreview(mSurfaceTexture, 0, 0);
        // TODO - Created
        mFilter.setTextureId(textureId);
        mFilter.onSurfaceCreated();
    }

    @SuppressLint("InlinedApi")
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

    public void takePhoto(View v) {
        mCamera.takePicture(null, null, new TakePictureWithFilterCallback());
    }

    private class TakePictureWithFilterCallback implements PictureCallback {

        private IntBuffer pixelBuffer;

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            final Semaphore waiter = new Semaphore(0);

            final int width = mSurfaceView.getMeasuredWidth();
            final int height = mSurfaceView.getMeasuredHeight();

            // Take picture on OpenGL thread
            final int[] pixelMirroredArray = new int[width * height];
            mRunAfterList.add(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "Begin");
                    pixelBuffer = IntBuffer.allocate(width * height);
                    GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
                    Log.e(TAG, "End");
                    waiter.release();
                }
            });
            mSurfaceView.requestRender();
            try {
                waiter.acquire();
            } catch (InterruptedException e) {
                Log.e(TAG, "waiter.acquire#" + e.getMessage());
            }
            // 重新启动预览
            camera.startPreview();
            //
            Log.e(TAG, "LSPictureCallback#Success");

            new Thread() {
                @Override
                public void run() {
                    super.run();
                    //
                    int[] pixelArray = pixelBuffer.array();
                    // Convert upside down mirror-reversed image to right-side up normal image.
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < width; j++) {
                            pixelMirroredArray[(height - i - 1) * width + j] = pixelArray[i * width + j];
                        }
                    }
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixelMirroredArray));
                    Log.e(TAG, "LSPictureCallback#bitmap=" + bitmap);
                    // 保存图片
                    saveBitmapToFile(bitmap, Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.jpg");
                }
            }.start();
        }
    }

    private void saveBitmapToFile(Bitmap bmp, String filePath) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.JPEG, 90, baos);
        byte[] data2 = baos.toByteArray();
        File jpgFile = new File(filePath);
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(jpgFile);
            outStream.write(data2);
            outStream.close();
        } catch (Exception e) {
            if (null != outStream)
                try {
                    outStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            Log.e(TAG, "saveBitmapToFile#Exception:" + e.getMessage());
        }
        Log.e(TAG, "saveBitmapToFile#end");
    }
}
