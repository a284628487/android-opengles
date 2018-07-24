package com.ccflying.glesvideo.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by ccfyyn on 17/9/23.
 */

public class Tools {

    public static FloatBuffer getFloatBuffer(float[] vertexes) {
        FloatBuffer buffer;
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexes.length * 4);

        vbb.order(ByteOrder.nativeOrder());// 设置字节顺序
        buffer = vbb.asFloatBuffer(); // 转换为Float型缓冲
        buffer.put(vertexes);// 向缓冲区中放入顶点坐标数据
        buffer.position(0); // 设置缓冲区起始位置
        return buffer;
    }

    public static ShortBuffer getShortBuffer(short[] vertexes) {
        ShortBuffer buffer;
        // short is 2 bytes, therefore we multiply the number if vertices with 2.
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexes.length * 2);

        vbb.order(ByteOrder.nativeOrder());// 设置字节顺序
        buffer = vbb.asShortBuffer(); // 转换为Float型缓冲
        buffer.put(vertexes);// 向缓冲区中放入顶点坐标数据
        buffer.position(0); // 设置缓冲区起始位置
        return buffer;
    }


    public static void saveBitmapFromByteBuffer(final ByteBuffer data,
                                                final int bmpWidth,
                                                final int bmpHeight,
                                                final String imgPath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(data);
                saveBitmap(bitmap, imgPath);
                data.clear();
            }
        }).start();
    }

    //图片保存
    public static boolean saveBitmap(final Bitmap b, final String mImgPath) {
        String path = mImgPath.substring(0, mImgPath.lastIndexOf("/") + 1);
        File folder = new File(path);
        if (!folder.exists() && !folder.mkdirs()) {
            return false;
        }
        long dataTake = System.currentTimeMillis();
        final String jpegName = path + dataTake + ".jpg";
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
