#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES vTexture;
varying vec2 textureCoordinate;

void main() {
    vec4 color = texture2D(vTexture, textureCoordinate);
    float rgb = color.g;
    if(textureCoordinate.x < 0.5) {
        gl_FragColor = color;
    }else{
        vec4 c = vec4(rgb, rgb, rgb, color.a);
        gl_FragColor = c;
    }
}
