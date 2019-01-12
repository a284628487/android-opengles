package com.ccf.glesapp.polygon;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ccf.glesapp.R;
import com.ccf.glesapp.polygon.baseshape.Circle;
import com.ccf.glesapp.polygon.baseshape.CircleColorFull;
import com.ccf.glesapp.polygon.baseshape.ColorRenderer;
import com.ccf.glesapp.polygon.baseshape.MultiColorTriangle;
import com.ccf.glesapp.polygon.baseshape.Square;
import com.ccf.glesapp.polygon.baseshape.TriangleMatrix;
import com.ccf.glesapp.polygon.baseshape.SingleColorTriangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PolygonActivity extends AppCompatActivity implements GLSurfaceView.Renderer {

    private GLSurfaceView sv;

    private GLSurfaceView.Renderer mInnerRenderer;

    private GL10 egl10;
    private int width, height;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInnerRenderer = new ColorRenderer(this);
        setRenderer();
    }

    private void setRenderer() {
        sv = new GLSurfaceView(this);
        setContentView(sv);
        sv.setEGLContextClientVersion(2);
        sv.setRenderer(this);
        sv.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mif = getMenuInflater();
        mif.inflate(R.menu.polygon_baseshape, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.color:
                mInnerRenderer = new ColorRenderer(this);
                break;
            case R.id.multicolor:
                mInnerRenderer = new MultiColorTriangle(this);
                break;
            case R.id.triangle:
                mInnerRenderer = new SingleColorTriangle(this);
                break;
            case R.id.triangle_matrix:
                mInnerRenderer = new TriangleMatrix(this);
                break;
            case R.id.circle:
                mInnerRenderer = new Circle(this);
                break;
            case R.id.color_circle:
                mInnerRenderer = new CircleColorFull(this);
                break;
            case R.id.square:
                mInnerRenderer = new Square(this);
                break;
        }
        setRenderer();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sv.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sv.onPause();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        egl10 = gl;
        mInnerRenderer.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        egl10 = gl;
        this.width = width;
        this.height = height;
        mInnerRenderer.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        egl10 = gl;
        mInnerRenderer.onDrawFrame(gl);
    }

}

// 实现GLSurfaceView的Render，在Render中完成三角形的绘制，具体行为有：
// 加载顶点和片元着色器。
// 确定需要绘制图形的坐标和颜色数据。
// 创建program对象，连接顶点和片元着色器，链接program对象。
// 设置视图窗口(ViewPort)。
// 将坐标数据颜色数据传入OpenGL ES程序中。
// 使颜色缓冲区的内容显示到屏幕上。
