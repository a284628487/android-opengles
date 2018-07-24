package com.ccflying.glesvideo;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.ccflying.glesvideo.filter.CameraFilter;
import com.ccflying.glesvideo.filter.GrayFilter;
import com.ccflying.glesvideo.util.Gl2Utils;
import com.ccflying.glesvideo.util.MatrixUtils;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    private final String TAG = "MainActivity";
    private GLSurfaceView mGLSurfaceView;
    private MediaPlayer mPlayer;
    private SurfaceTexture mSurfaceTexture;
    //
    private int textureId;
    //
    private CameraFilter mFilter;

    private boolean stoped = false;
    //
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (stoped)
                return;
            sendEmptyMessageDelayed(0, 20);
            if (null != mGLSurfaceView) {
                mGLSurfaceView.requestRender();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGLSurfaceView = findViewById(R.id.texture);
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //
        mPlayer = new MediaPlayer();
        mPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
        mFilter = new GrayFilter(getResources(), true);
        try {
            mPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/video.mp4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startPlay() {
        try {
            mPlayer.setSurface(new Surface(mSurfaceTexture));
            mPlayer.prepare();
            mPlayer.start();
            mHandler.sendEmptyMessageDelayed(0, 16);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stoped = true;
        stopPlay();
    }

    private void stopPlay() {
        mPlayer.stop();
        mPlayer.release();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textureId = createTextureID();
        mSurfaceTexture = new SurfaceTexture(textureId);
        startPlay();
        // TODO
        mFilter.setTextureId(textureId);
        mFilter.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float[] matrix = new float[16];
        Gl2Utils.getShowMatrix(matrix, 512, 288, width, height);
        String s0 = "";
        for (int i = 0; i < matrix.length; i++) {
            s0 += matrix[i] + ", ";
        }
        // 2.7654321, 0.0, 0.0, 0.0,
        // 0.0, 1.0, 0.0, 0.0,
        // 0.0, 0.0, -1.0, 0.0,
        // 0.0, 0.0, -1.0, 1.0,
        Log.e(TAG, s0);

        MatrixUtils.rotate(matrix, 90);
        String s = "";
        for (int i = 0; i < matrix.length; i++) {
            s += matrix[i] + ", ";
        }
        // -1.2088088E-7, 1.0, 0.0, 0.0,
        // -2.7654321, -4.371139E-8, -0.0, 0.0,
        // 0.0, 0.0, -1.0, 0.0,
        // 0.0, 0.0, -1.0, 1.0,
        Log.e(TAG, s);
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
}
