#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES vTexture;
varying vec2 textureCoordinate;

void main() {
    vec4 color = texture2D(vTexture, textureCoordinate);
    gl_FragColor = 2.0 * color;
}
