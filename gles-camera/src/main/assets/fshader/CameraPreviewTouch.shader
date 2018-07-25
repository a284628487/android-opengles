#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES vTexture;
varying vec2 textureCoordinate;

varying vec2 fTouch;
uniform float aXY;

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

    if(distance(fTouch, vec2(textureCoordinate.x, textureCoordinate.y / aXY)) < 0.10) {
        gl_FragColor = changeColor(oriColor);
        return;
    }
    gl_FragColor = oriColor;
}