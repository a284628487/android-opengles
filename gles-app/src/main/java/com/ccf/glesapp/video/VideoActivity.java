package com.ccf.glesapp.video;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;

import com.ccf.glesapp.stream.StreamFilter;
import com.ccf.glesapp.util.Gl2Utils;
import com.ccf.glesapp.stream.videofilter.GrayFilter;
import com.ccf.glesapp.util.Utils;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    private final String TAG = "VideoActivity";
    private GLSurfaceView mGLSurfaceView;
    private MediaPlayer mPlayer;
    private SurfaceTexture mSurfaceTexture;
    //
    private int textureId;
    //
    private StreamFilter mFilter;

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGLSurfaceView = new GLSurfaceView(this);
        setContentView(mGLSurfaceView);
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
        mFilter = new GrayFilter(getResources());
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
        textureId = Utils.createStreamTexture();
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
        int w = 640;
        int h = 360;
        Gl2Utils.getShowMatrix(matrix, w, h, width, height);
        mFilter.setMatrix(matrix);
    }

    // un-reached
    private void roatete90(int width, int height) {
        float[] matrix = new float[16];
        int w = 640;
        int h = 360;
        Gl2Utils.rotate(matrix, 90);
        Gl2Utils.getShowMatrix(matrix, w, h, height, width);
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

}
