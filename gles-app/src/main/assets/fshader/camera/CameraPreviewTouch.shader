#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES vTexture;
varying vec2 textureCoordinate;

varying vec2 fTouch;
uniform float aXY;

varying vec2 varyPosition;

vec4 changeColor(vec4 inputColor) {
    float r = (inputColor.r > 0.55) ? 1.0 : 0.0;
    float g = (inputColor.g > 0.55) ? 1.0 : 0.0;
    float b = (inputColor.b > 0.55) ? 1.0 : 0.0;
    if(r + g < 2.0) {
        return vec4(0, 0, 0, 0);
    }
    return vec4(1, 1, 1, 0);
}

void main() {
    vec4 oriColor = texture2D(vTexture, textureCoordinate);

    if(distance(vec2(fTouch.x, fTouch.y * aXY), vec2(varyPosition.x, varyPosition.y * aXY)) < 0.20) {
        gl_FragColor = changeColor(oriColor);
        return;
    }
    gl_FragColor = oriColor;
}