#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES vTexture;
varying vec2 textureCoordinate;

varying vec2 fCenter;

void main() {
    vec4 color = texture2D(vTexture, textureCoordinate);
    float flashScale = 2.0 - distance(vec2(textureCoordinate.x, textureCoordinate.y * 0.6), vec2(fCenter.x, fCenter.y * 0.6)) * 6.0;
    if (flashScale < 0.3) {
        flashScale = 0.3;
    }
    gl_FragColor = flashScale * color;
}
