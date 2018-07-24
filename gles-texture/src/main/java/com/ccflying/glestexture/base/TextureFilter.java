package com.ccflying.glestexture.base;

public enum TextureFilter {

    NONE(0, new float[]{0.0f, 0.0f, 0.0f}), // 无
    GRAY(1, new float[]{0.299f, 0.587f, 0.114f}), // 灰度处理
    COOL(2, new float[]{0.0f, 0.0f, 0.1f}), // 冷色调
    WARM(2, new float[]{0.1f, 0.1f, 0.0f}), // 暖色调
    BLUR(3, new float[]{0.006f, 0.004f, 0.002f}), // 模糊
    MAGN(4, new float[]{0.0f, 0.0f, 0.4f}); // 放大

    private int vChangeType;
    private float[] data;

    TextureFilter(int vChangeType, float[] data) {
        this.vChangeType = vChangeType;
        this.data = data;
    }

    public int getType() {
        return vChangeType;
    }

    public float[] data() {
        return data;
    }
}
