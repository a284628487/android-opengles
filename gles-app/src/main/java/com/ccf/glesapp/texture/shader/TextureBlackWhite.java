package com.ccf.glesapp.texture.shader;

import android.content.Context;

public class TextureBlackWhite extends TextureShape {

    public TextureBlackWhite(Context context) {
        super(context, "vshader/TextureBlackWhite.shader", "fshader/TextureBlackWhite.shader");
    }

}
