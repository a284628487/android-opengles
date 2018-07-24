package com.ccflying.glestexture;

import android.opengl.GLSurfaceView;

public class TextureBlackWhite extends TextureShape {

    public TextureBlackWhite(GLSurfaceView view) {
        super(view, "vshader/TextureBlackWhite.shader", "fshader/TextureBlackWhite.shader");
    }

}
