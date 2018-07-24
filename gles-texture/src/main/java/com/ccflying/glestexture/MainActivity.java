package com.ccflying.glestexture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ccflying.glestexture.base.TextureFilter;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView mSurfaceView;

    final String TAG = "MainActivity";

    private Bitmap loadBitmap() {
        InputStream ins = null;
        try {
            ins = getAssets().open("mm.png");
            Bitmap bmp = BitmapFactory.decodeStream(ins);
            return bmp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_main);
        mSurfaceView = new GLSurfaceView(this);
        mSurfaceView.setEGLContextClientVersion(2);
        // setTextureRenderer();
        // setTextureFilterRenderer();
        // setBlackWhiteFilter();
        setOverlayTexture();
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mSurfaceView);
    }

    protected void setOverlayTexture() {
        TextureOverlay olayRender = new TextureOverlay(mSurfaceView);
        mSurfaceView.setRenderer(olayRender);
    }

    protected void setBlackWhiteFilter() {
        TextureBlackWhite renderer = new TextureBlackWhite(mSurfaceView);
        renderer.setBitmap(loadBitmap());
        mSurfaceView.setRenderer(renderer);
    }

    protected void setTextureFilterRenderer() {
        TextureWithFilter mRenderer = new TextureWithFilter(mSurfaceView);
        mRenderer.setFilter(TextureFilter.GRAY);
        mRenderer.setBitmap(loadBitmap());
        mSurfaceView.setRenderer(mRenderer);
    }


    protected void setTextureRenderer() {
        TextureShape mRenderer = new TextureShape(mSurfaceView);
        mRenderer.setBitmap(loadBitmap());
        mSurfaceView.setRenderer(mRenderer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }
}
