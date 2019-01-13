package com.ccf.glesapp.matrix;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.ccf.glesapp.util.Utils;
import com.ccf.glesapp.util.VaryTools;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// https://blog.csdn.net/junzia/article/details/53154175
public class MatrixRender extends BaseShape {
    //
    private VaryTools tools;

    final float[] cubePositions = { //
            -1.0f, 1.0f, 1.0f,// 正面左上0
            -1.0f, -1.0f, 1.0f,// 正面左下1
            1.0f, -1.0f, 1.0f,// 正面右下2
            1.0f, 1.0f, 1.0f,// 正面右上3
            -1.0f, 1.0f, -1.0f,// 背面左上4
            -1.0f, -1.0f, -1.0f,// 背面左下5
            1.0f, -1.0f, -1.0f,// 背面右下6
            1.0f, 1.0f, -1.0f,// 背面右上7
    };

    final short[] indexes = { //
            0, 3, 2, 0, 2, 1, // 正面
            0, 1, 5, 0, 5, 4, // 左面
            0, 7, 3, 0, 4, 7, // 上面
            6, 7, 4, 6, 4, 5, // 后面
            6, 3, 7, 6, 2, 3, // 右面
            6, 5, 1, 6, 1, 2 // 下面
    };

    // 八个顶点的颜色，与顶点坐标一一对应
    final float colors[] = { //
            0f, 1f, 0f, 1f, // g
            0f, 1f, 0f, 1f,// g
            0f, 1f, 0f, 1f,// g
            0f, 1f, 0f, 1f,// g
            1f, 0f, 0f, 1f,// r
            1f, 0f, 0f, 1f,// r
            1f, 0f, 0f, 1f,// r
            1f, 0f, 0f, 1f,// r
    };

    private int mProgram;
    private int vMatrixHandle;
    private int aColorHandle;
    private int vPositionHandle;

    private FloatBuffer mPositionBuffer;
    private FloatBuffer mColorBuffer;
    private ShortBuffer mIndexesBuffer;

    private String vShaderText, fShaderText;

    public MatrixRender(GLSurfaceView view) {
        super(view);
        tools = new VaryTools();
        vShaderText = Utils.getAssetsResourceString(view.getResources(), "vshader/MatrixRender.shader");
        fShaderText = Utils.getAssetsResourceString(view.getResources(), "fshader/MatrixRender.shader");
    }

    private float[] mMVPMatrix = new float[16];

    @Override
    public void onDrawFrame(GL10 arg0) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        //
        vMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        aColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        vPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // ----------------begin----------------
        // 1.原图
        setMatrix(tools.getFinalMatrix());
        drawSelf();

        // y轴正方形平移
        tools.pushMatrix();
        tools.translate(0, 3, 0);
        setMatrix(tools.getFinalMatrix());
        drawSelf();
        tools.popMatrix();

        // y轴负方向平移，然后按xyz->(0,0,0)到(1,1,1)旋转30度
        tools.pushMatrix();
        tools.translate(0, -3, 0);
        tools.rotate(30f, 1, 1, 1);
        setMatrix(tools.getFinalMatrix());
        drawSelf();
        tools.popMatrix();

        // x轴负方向平移，然后按xyz->(0,0,0)到(1,-1,1)旋转120度，在放大到0.5倍
        tools.pushMatrix();
        tools.translate(-2.5f, 0, 0);
        tools.scale(0.5f, 0.5f, 0.5f);

        // 在以上变换的基础上再进行变换
        tools.pushMatrix();
        tools.translate(10, 0, 0);
        tools.scale(1.0f, 2.0f, 1.0f);
        tools.rotate(30f, 1, 2, 1);
        setMatrix(tools.getFinalMatrix());
        drawSelf();
        tools.popMatrix();

        // 接着被中断的地方执行
        tools.rotate(30f, -1, -1, 1);
        setMatrix(tools.getFinalMatrix());
        drawSelf();
        tools.popMatrix();
    }

    private void setMatrix(float[] m) {
        mMVPMatrix = m;
    }

    public void drawSelf() {
        // 将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram);
        // 指定vMatrix的值
        if (mMVPMatrix != null) {
            GLES20.glUniformMatrix4fv(vMatrixHandle, 1, false, mMVPMatrix, 0);
        }
        // 启用句柄
        GLES20.glEnableVertexAttribArray(vPositionHandle);
        GLES20.glEnableVertexAttribArray(aColorHandle);
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(vPositionHandle, 3, GLES20.GL_FLOAT,
                false, 0, mPositionBuffer);
        // 设置绘制三角形的颜色
        GLES20.glVertexAttribPointer(aColorHandle, 4, GLES20.GL_FLOAT, false,
                0, mColorBuffer);
        // 索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexes.length,
                GLES20.GL_UNSIGNED_SHORT, mIndexesBuffer);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(vPositionHandle);
        // 禁用颜色数组句柄
        GLES20.glDisableVertexAttribArray(aColorHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float rate = width / (float) height;
        // Matrix.orthoM
        tools.ortho(-rate * 6, rate * 6, -6, 6, 3, 20);
        // Matrix.setLookAtM
        tools.setCamera(0, 0, 10, 0, 0, 0, 0, 1, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        // 开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        //
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vShaderText);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fShaderText);
        // 创建一个OpenGLES程序
        mProgram = GLES20.glCreateProgram();
        // 添加顶点着色器
        GLES20.glAttachShader(mProgram, vertexShader);
        // 添加片元着色器
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 连接着色器程序
        GLES20.glLinkProgram(mProgram);
        //
        mIndexesBuffer = allocateShortBuffer(indexes);
        mPositionBuffer = allocateFloatBuffer(cubePositions);
        mColorBuffer = allocateFloatBuffer(colors);
    }

}
