package com.ccf.glesapp.matrix;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public abstract class BaseShape implements Renderer {

	protected GLSurfaceView mView;
	
	public static final int COORDS_PER_VERTEX = 3;

	public BaseShape(GLSurfaceView view) {
		this.mView = view;
	}

	public int loadShader(int type, String shaderCode) {
		// 根据type创建顶点着色器或者片元着色器
		int shader = GLES20.glCreateShader(type);
		// 将资源加入到着色器中并编译
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		return shader;
	}

	public FloatBuffer allocateFloatBuffer(float[] input) {
		// float占4个字节，所以是length * 4，
		ByteBuffer floatBB = ByteBuffer.allocateDirect(input.length * 4);
		floatBB.order(ByteOrder.nativeOrder());
		FloatBuffer floatBuffer = floatBB.asFloatBuffer();
		floatBuffer.put(input);
		floatBuffer.position(0);
		return floatBuffer;
	}

	public ShortBuffer allocateShortBuffer(short[] input) {
		// short占2个字节，所以是length * 2，
		ByteBuffer floatBB = ByteBuffer.allocateDirect(input.length * 2);
		floatBB.order(ByteOrder.nativeOrder());
		ShortBuffer shortBuffer = floatBB.asShortBuffer();
		shortBuffer.put(input);
		shortBuffer.position(0);
		return shortBuffer;
	}
}
