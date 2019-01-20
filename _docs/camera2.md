
## **CameraManager**

Camera管理器，使用该管理器来连接Camera服务，打开Camera操作。

```java
cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
```

## **CameraDevice**

表示Android设备上的一个Camera对象，可以通过它来拍照获取图像或者其它处理。

## **CameraCharacteristics**

该类代表一个CameraDevice的属性配置，在一个CameraDevice上这些属性是固定的。
可以通过`CameraManager#getCameraCharacteristics(String cameraId)`获取该对象。


## step1: 获取指定的Camera(前置/后置)，并且获取预览大小

```java
// 获取所有的CameraId
String[] cids = cameraManager.getCameraIdList();
for (String cid : cids) {
    // 获取CameraDevice的属性。
    CameraCharacteristics stics = cameraManager.getCameraCharacteristics(cid);
    // 获取前置 / 后置
    Integer facing = stics.get(CameraCharacteristics.LENS_FACING);
    int excludeCameraId = (mIsBackCamera ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK);
    if (null != facing && facing == excludeCameraId)
        continue;
    cameraId = cid;
    // 获得Camera的预览配置
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
```

## step2: openCamera

```java
cameraManager.openCamera(cameraId, cameraOpenCallback, mCameraHandler);
```

> 打开Camera，打开成功后通过**CameraDevice.StateCallback**回调。 

## step3: CameraDevice.StateCallback#onOpened

在**Callback#onOpend**方法中，获取**CameraDevice**对象。
```java
private CameraDevice.StateCallback cameraOpenCallback = new CameraDevice.StateCallback() {
    @Override
    public void onOpened(@NonNull CameraDevice camera) {
        cameraDevice = camera;
        createCaptureSession();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
    }

    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
    }
};
```

## step4: createCaptureSession

- 创建 **CaptureRequest.Builder**

```java
builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
builder.addTarget(mSurface);
```

- 创建 **CaptureSession**，也是通过 callback 来回调处理。

```java
cameraDevice.createCaptureSession(Arrays.asList(mSurface), captureSessionCallback, mCameraHandler);
```

### CameraCaptureSession.StateCallback

```java
private CameraCaptureSession.StateCallback captureSessionCallback = new CameraCaptureSession.StateCallback() {
    @Override
    public void onConfigured(@NonNull CameraCaptureSession session) {
        captureSession = session;
        startPreview();
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
    }
};
```
> 获取到**CameraCaptureSession**

## step5: startPreview

```java
private void startPreview() {
    try {
        builder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        // 使用CameraCaptureSession创建持续请求.
        captureSession.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                           @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                // 调用 GLSurfaceView.requestRender() 请求刷新渲染。
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

```
