package com.ccf.glesapp.polygon.baseshape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.ccf.glesapp.util.Utils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleMatrix implements GLSurfaceView.Renderer {

    private final static String TAG = "TriangleMatrix";
    // 顶点着色器(增加了矩阵变换)";
    // uniform一般用于对同一组顶点组成的3D物体中各个顶点都相同的量
    // varying一般用于从顶点着色器传入到片元着色器的量
    // attribute一般用于每个顶点都各不相同的量
    private String vShader = "attribute vec4 vPosition;\n" +
            "uniform mat4 vMatrix;\n" +
            "varying vec4 fColor;\n" +
            "attribute vec4 vColor;\n" +
            "void main() {\n" +
            "    gl_Position = vMatrix * vPosition;\n" +
            "    fColor = vColor;\n" +
            "}";
    private String fShader = "precision mediump float;\n" +
            "varying vec4 fColor;\n" +
            "void main() {\n" +
            "    gl_FragColor = fColor;\n" +
            "}";
    //
    private int mProgram;
    // 绘制顺序为逆时针
    private float[] mCoordinates = { //
            0f, 0.5f, 0f, //
            -0.5f, -0.5f, 0f, //
            0.5f, -0.5f, 0f
    };

    // 投影转换
    private float[] mMVPMatrix = new float[16];

    // RGBA
    private float[] mColors = { //
            0.0f, 1.0f, 0.0f, 1.0f, //
            1.0f, 0.0f, 0.0f, 1.0f, //
            0.0f, 0.0f, 1.0f, 1.0f //
    };

    // 坐标底层数据
    private FloatBuffer mGLPositionBuffer;
    // 颜色底层数据
    private FloatBuffer mGLColorBuffer;
    //
    private int mGLPositionHandle, mGLColorHandle, mGLMatrixHandle;
    // 每个顶点坐标个数
    private static final int COORDS_PER_VERTEX = 3;
    // 每个颜色值数值个数
    private static final int COLOR_PER_VERTEX = 4;

    // 顶点偏移量，即每个顶点在Buffer中所占的byte位数。
    private int vertexStride = COORDS_PER_VERTEX * 4;
    // 顶点个数
    private int vertexCount = mCoordinates.length / COORDS_PER_VERTEX;

    public TriangleMatrix(Context context) {
        // 申请底层空间, 将坐标数据转换为FloatBuffer, 用以传入给OpenGL ES程序
        mGLPositionBuffer = Utils.allocateFloatBuffer(mCoordinates);
        mGLColorBuffer = Utils.allocateFloatBuffer(mColors);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 将背景设置为灰色
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        // 创建vShader
        int vShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShaderId, vShader);
        GLES20.glCompileShader(vShaderId);
        // 创建fShader
        int fShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShaderId, fShader);
        GLES20.glCompileShader(fShaderId);
        // 创建项目
        mProgram = GLES20.glCreateProgram();
        // 将顶点着色器加入到程序
        GLES20.glAttachShader(mProgram, vShaderId);
        // 将片元着色器加入到程序中
        GLES20.glAttachShader(mProgram, fShaderId);
        // 连接到着色器程序
        GLES20.glLinkProgram(mProgram);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float radio = width * 1f / height;
        // 设置透视投影
        // Matrix.frustumM(m, offset, left, right, bottom, top, near, far)
        float[] mProjectMatrix = new float[16];
        float[] mViewMatrix = new float[16];
        // 设置相机位置
        // Matrix.setLookAtM (float[] rm, // 接收相机变换的矩阵
        // int rmOffset, // 变换矩阵的起始位置（偏移量）
        // float eyeX, float eyeY, float eyeZ, // 相机位置
        // float centerX, float centerY, float centerZ, // 观测点位置(绘制目标中心点位置)
        // float upX, float upY, float upZ) // up向量在xyz上的分量
        Matrix.setLookAtM(mViewMatrix, 0,
                0, 0, 4.0f,
                0f, 0f, 0f,
                0f, 1f, 0f);
        // near和far参数稍抽象一点，就是一个立方体的前面和后面，near和far需要结合拍摄相机即观察者眼睛的位置来设置，
        // 例如setLookAtM中设置eyeX = 0, eyeY = 0, eyeZ = 4，near设置的范围需要是小于 4 才可以看得到绘制的图像，
        // 如果大于4，图像就会处于了观察者眼睛的后面，这样绘制的图像就会消失在镜头前，
        // far参数，far参数影响的是立体图形的背面，far一定比near大，一般会设置得比较大，
        // 如果设置的比较小，一旦3D图形尺寸很大，这时候由于far太小，这个投影矩阵没法容纳图形全部的背面，这样3D图形的背面会有部分隐藏掉的。
        // eg: radio[-0.4f, 0.4f]; 将 X 方向上的坐标单位比例，变换成与 Y 方向上统一。
        Matrix.frustumM(mProjectMatrix, 0, -radio, radio, -1, 1, 3, 7);
        // 计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        // https://blog.csdn.net/tanmx219/article/details/81407264
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        // 将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        // 获取变换矩阵vMatrix成员句柄
        mGLMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        // 指定vMatrix值
        GLES20.glUniformMatrix4fv(mGLMatrixHandle, 1, false, mMVPMatrix, 0);
        // 获取顶点着色器的vPosition成员句柄
        mGLPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mGLPositionHandle);
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mGLPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, mGLPositionBuffer);
        // 获取片元着色器的vColor成员的句柄
        mGLColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
        // 传递颜色
        GLES20.glEnableVertexAttribArray(mGLColorHandle);
        // 传递 attribute 类型数值。
        GLES20.glVertexAttribPointer(mGLColorHandle, COLOR_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, mGLColorBuffer);
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mGLPositionHandle);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mGLColorHandle);
    }
}