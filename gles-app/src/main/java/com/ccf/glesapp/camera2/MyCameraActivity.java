package com.ccf.glesapp.camera2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.ccf.glesapp.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class MyCameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {

    private String TAG = "MyCameraActivity";

    private AutoFitTextureView textureView;

    private CameraDevice mCameraDevice;

    private CameraManager cameraManager;

    private String mCameraId;

    private Size mPreviewSize;

    private Surface mSurface;

    private Semaphore mCameraLock = new Semaphore(1); //Camera互斥锁

    private CameraDevice.StateCallback openCameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            try {
                // 创建用于预览和拍照的CameraCaptureSession
                mCameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), sessionCallback, mCameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    private Handler mCameraHandler;

    private CameraCaptureSession mCameraSession;

    private ImageReader mImageReader;

    private File mFile;

    private int mSensorOrientation;

    //Sensor方向，大多数设备是90度
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    //Sensor方向，一些设备是270度
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;

    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    //sensor的方向为90度时，屏幕方向与Sensor方向的对应关系
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //sensor的方向为270度时，屏幕方向与Sensor方向的对应关系
    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private Matrix transform = new Matrix();

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Log.w(TAG, "onCaptureStarted: ");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.w(TAG, "onCaptureProgressed: ");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.w(TAG, "onCaptureCompleted: ");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            Log.w(TAG, "onCaptureSequenceCompleted: ");
        }
    };

    private CameraCaptureSession.StateCallback sessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            mCameraSession = session;
            // 开启预览
            startPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }
    };

    private void startPreview() {
        try {
            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(mSurface);

            // 预览裁剪 - begin
            // Rect r = new Rect(0, 0, 500, 500);
            // builder.set(CaptureRequest.SCALER_CROP_REGION, r);
            // 预览裁剪 - end

            /**
             int[] elements = new int[]{
             1, 2, 1, 5, 1, 3,
             1, 2, 1, 4, 1, 4,
             1, 2, 1, 3, 1, 5};

             ColorSpaceTransform cst = new ColorSpaceTransform(elements);
             builder.set(CaptureRequest.COLOR_CORRECTION_MODE, CaptureRequest.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX);
             builder.set(CaptureRequest.COLOR_CORRECTION_TRANSFORM, cst);
             // builder.set(CaptureRequest.JPEG_ORIENTATION, 180);
             */

            mCameraSession.setRepeatingRequest(builder.build(), captureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //获取捕获的照片数据
            Log.e(TAG, "onImageAvailable : " + reader);
            Image image = reader.acquireNextImage();
            mCameraHandler.post(new SaveImageRunnable(MyCameraActivity.this, image, mFile));
        }
    };

    private HandlerThread thread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textureView = new AutoFitTextureView(this);
        setContentView(textureView);

        textureView.setSurfaceTextureListener(this);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        thread = new HandlerThread("wtf");
        thread.start();
        mCameraHandler = new Handler(thread.getLooper());

        chooseCameraId();

        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mCameraHandler);
    }

    protected void chooseCameraId() {
        try {
            String[] cids = cameraManager.getCameraIdList();
            for (String cameraId : cids) {
                CameraCharacteristics stics = cameraManager.getCameraCharacteristics(cameraId);
                int facing = stics.get(CameraCharacteristics.LENS_FACING);
                if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                    mCameraId = cameraId;
                    //
                    StreamConfigurationMap map = stics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    Size[] sizes = map.getOutputSizes(ImageFormat.JPEG);
                    for (int i = 0; i < sizes.length; i++) {
                        Size s = sizes[i];
                        Log.w(TAG, "chooseCameraId: " + s.getWidth() + " x " + s.getHeight());
                    }
                    //
                    mPreviewSize = new Size(1280, 720);

                    //获得Sensor方向
                    mSensorOrientation = stics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        try {
            // /** 1. transform
            int bw = width;
            int bh = height;
            float[] src = {0, 0, 0, bh, bw, bh, bw, 0};
            int DX = 100;
            float[] dst = {0 + DX, 0, 0, bh, bw, bh, bw - DX, 0};
            transform.setPolyToPoly(src, 0, dst, 0, 4);
            // */

            /** 2. scale
             transform.setScale(0.8f, 0.8f, 0.5f, 0.5f);
             transform.postTranslate(width * 0.1f, height * 0.1f);
             */

            /** 3. other
             transform.setSinCos(1, 0, width / 2, height / 2);
             */
            textureView.setTransform(transform);

            surface.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mSurface = new Surface(surface);
            cameraManager.openCamera(mCameraId, openCameraCallback, mCameraHandler);
            textureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void savePicture() {
        Bitmap bmp = textureView.getBitmap();
        if (null == bmp) {
            return;
        }
        // https://blog.csdn.net/nupt123456789/article/details/24600055
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), transform, true);
        //
        File f = new File(Environment.getExternalStorageDirectory(), "test2.png");
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(convertBitmap2Bytes(bmp));
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "" + e);
        }
    }

    public static byte[] convertBitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 拍照
     */
    private void takePicture() {
        //照片保存路径
        mFile = new File(Environment.getExternalStorageDirectory(), getFileName(true));

        try {
            //创建作为拍照的CaptureRequest.Builder
            CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //将mImageReader的surface作为CaptureRequest.Builder的目标
            captureRequestBuilder.addTarget(mImageReader.getSurface());

            // 设置自动对焦模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //设置自动曝光模式
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            //获得屏幕方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            switch (mSensorOrientation) {
                //Sensor方向为90度时
                case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                    captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, DEFAULT_ORIENTATIONS.get(rotation));
                    break;
                //Sensor方向为270度时
                case SENSOR_ORIENTATION_INVERSE_DEGREES:
                    captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, INVERSE_ORIENTATIONS.get(rotation));
                    break;
            }

            //创建拍照的CameraCaptureSession.CaptureCallback对象
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                //在拍照完成时调用
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Toast.makeText(MyCameraActivity.this, "保存到：" + mFile.toString(), Toast.LENGTH_SHORT).show();
                    startPreview(); // 继续预览
                }
            };
            // 停止连续取景
            mCameraSession.stopRepeating();
            // 捕获静态图像
            mCameraSession.capture(captureRequestBuilder.build(), captureCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera2_nogles, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.takePic) {
            savePicture();
        } else if (item.getItemId() == R.id.takePic2) {
            takePicture();
        }
        return super.onOptionsItemSelected(item);
    }

    private void closeSession() {
        if (null != mCameraSession) {
            mCameraSession.close();
            mCameraSession = null;
        }
    }

    private void closeDevice() {
        try {
            mCameraLock.acquire();
            //
            closeSession();
            // 关闭相机
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            // 关闭ImageReader
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mCameraLock.release();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //
        closeSession();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //
        closeDevice();
        //
        stopThread();
    }

    protected void stopThread() {
        thread.quitSafely();
        try {
            thread.join();
            thread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            throw new RuntimeException("停止后台线程时中断");
        }
    }

    public class SaveImageRunnable implements Runnable {

        private Context context;
        private final Image mImage;//要保存的图片数据
        private final File mFile;//保存到的文件

        public SaveImageRunnable(Context context, Image image, File file) {
            this.context = context;
            mImage = image;
            mFile = file;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            Log.e("FileUtil", "thread name:" + Thread.currentThread().getName());
            BufferedOutputStream bos = null;
            try {
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes); // 读取数据到bytes中

                bos = new BufferedOutputStream(new FileOutputStream(mFile));
                bos.write(bytes); // 将 b.length 个字节从指定 bytes 数组写入此文件输出流中
                bos.flush();
            } catch (IOException e) {
                Toast.makeText(context, "SaveFailed", Toast.LENGTH_SHORT).show();
            } finally {
                mImage.close();
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static String getFileName(boolean isPicture) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String datetime = formatter.format(new Date(System.currentTimeMillis()));
        //若是图片
        if (isPicture) {
            return "IMG_" + datetime + ".jpg";
        } else {
            //若是视频
            return "VID_" + datetime + ".mp4";
        }
    }
}
