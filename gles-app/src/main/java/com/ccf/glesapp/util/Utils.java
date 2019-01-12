package com.ccf.glesapp.util;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Utils {

    final static String TAG = "Utils";

    // 通过路径加载Assets中的文本内容
    public static String getAssetsResourceString(Resources mRes, String path) {
        StringBuilder result = new StringBuilder();
        try {
            InputStream is = mRes.getAssets().open(path);
            int ch;
            byte[] buffer = new byte[1024];
            while (-1 != (ch = is.read(buffer))) {
                result.append(new String(buffer, 0, ch));
            }
        } catch (Exception e) {
            return null;
        }
        return result.toString().replaceAll("\\r\\n", "\n");
    }

    public static int createProgram(String vertex, String fragment) {
        int vertexId = loadShader(GLES20.GL_VERTEX_SHADER, vertex);
        if (vertexId == 0) {
            return 0;
        }

        int fragmentId = loadShader(GLES20.GL_FRAGMENT_SHADER, fragment);
        if (fragmentId == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();

        if (program != 0) {
            GLES20.glAttachShader(program, vertexId);
            checkGLError("Attach Vertex Shader");
            GLES20.glAttachShader(program, fragmentId);
            checkGLError("Attach Fragment Shader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG,
                        "Could not link program:"
                                + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    public static int loadShader(int type, String shaderCode) {
        // 根据type创建顶点着色器或者片元着色器
        int shader = GLES20.glCreateShader(type);
        // 将资源加入到着色器中并编译
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int compiled[] = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader:" + type);
            Log.e(TAG, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    public static FloatBuffer allocateFloatBuffer(float[] input) {
        // float占4个字节，所以是length * 4，
        ByteBuffer floatBB = ByteBuffer.allocateDirect(input.length * 4);
        floatBB.order(ByteOrder.nativeOrder());
        FloatBuffer floatBuffer = floatBB.asFloatBuffer();
        floatBuffer.put(input);
        floatBuffer.position(0);
        return floatBuffer;
    }

    public static ShortBuffer allocateShortBuffer(short[] input) {
        // short占2个字节，所以是length * 2，
        ByteBuffer floatBB = ByteBuffer.allocateDirect(input.length * 2);
        floatBB.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = floatBB.asShortBuffer();
        shortBuffer.put(input);
        shortBuffer.position(0);
        return shortBuffer;
    }

    public static void checkGLError(String op) {
    }

}
