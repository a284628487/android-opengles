package com.ccf.glesapp.texture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ccf.glesapp.R;
import com.ccf.glesapp.texture.base.TextureFilter;
import com.ccf.glesapp.texture.shader.TextureBlackWhite;
import com.ccf.glesapp.texture.shader.TextureOverlay;
import com.ccf.glesapp.texture.shader.TextureShape;
import com.ccf.glesapp.texture.shader.TextureWithFilter;

import java.io.IOException;
import java.io.InputStream;

public class TextureActivity extends AppCompatActivity {

    private GLSurfaceView mSurfaceView;

    final String TAG = "MainActivity";

    private Bitmap loadBitmap() {
        try {
            InputStream ins = getAssets().open("mm.png");
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
        setTextureRenderer();
    }

    private void createSurfaceView() {
        mSurfaceView = new GLSurfaceView(this);
        mSurfaceView.setEGLContextClientVersion(2);
    }

    private void setView() {
        mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setContentView(mSurfaceView);
    }

    protected void setOverlayTexture() {
        createSurfaceView();
        TextureOverlay overlayRender = new TextureOverlay(this);
        mSurfaceView.setRenderer(overlayRender);
        setView();
    }

    protected void setBlackWhiteFilter() {
        createSurfaceView();
        TextureBlackWhite renderer = new TextureBlackWhite(this);
        renderer.setBitmap(loadBitmap());
        mSurfaceView.setRenderer(renderer);
        setView();
    }

    protected void setTextureFilterRenderer() {
        createSurfaceView();
        TextureWithFilter mRenderer = new TextureWithFilter(this);
        mRenderer.setFilter(TextureFilter.MAGN);
        mRenderer.setBitmap(loadBitmap());
        mSurfaceView.setRenderer(mRenderer);
        setView();
    }

    protected void setTextureRenderer() {
        createSurfaceView();
        TextureShape mRenderer = new TextureShape(this);
        mRenderer.setBitmap(loadBitmap());
        mSurfaceView.setRenderer(mRenderer);
        setView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mif = getMenuInflater();
        mif.inflate(R.menu.texture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.texture:
                setTextureRenderer();
                break;
            case R.id.overlay_texture:
                setOverlayTexture();
                break;
            case R.id.black_white:
                setBlackWhiteFilter();
                break;
            case R.id.texture_filter:
                setTextureFilterRenderer();
                break;
        }
        return super.onOptionsItemSelected(item);
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
