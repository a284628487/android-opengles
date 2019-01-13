package com.ccf.glesapp.camera2;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

// com.ccflying.glescamera2.MyGLSurfaceView
public class MyGLSurfaceView extends GLSurfaceView {

    final String TAG = "MyGLSurfaceView";

    private int pW, pH;

    public MyGLSurfaceView(Context context) {
        super(context);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setWidthHeight(int previewWidth, int previewHeight) {
        pW = previewWidth;
        pH = previewHeight;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        Log.e(TAG, "(" + w + ", " + h + "):(" + pW + ", " + pH + ")");
        if (0 == pW || 0 == pH) {
            setMeasuredDimension(w, h);
        } else {
            // 预览宽高比大于view
            if (w * 1f / h > pW * 1f / pH) {
                setMeasuredDimension(w, pH * w / pW);
            } else { // 小于
                setMeasuredDimension(pW * h / pH, h);
            }
        }
    }
}
