#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES vTexture;
varying vec2 textureCoordinate;
// Flash
uniform float flashScale;

void main() {
    vec4 color = texture2D(vTexture, textureCoordinate);
    gl_FragColor = flashScale * color;
}
