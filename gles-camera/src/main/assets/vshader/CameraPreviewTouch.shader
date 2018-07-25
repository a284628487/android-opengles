uniform mat4 vMatrix;

attribute vec4 vPosition;

attribute vec2 vCoord;
varying vec2 textureCoordinate;

varying vec2 fTouch;
attribute vec2 vTouch;

void main() {
    gl_Position = vMatrix * vPosition;
    textureCoordinate = vCoord;
    vec4 vtmp = vec4(vTouch, 0, 0);
    fTouch = (vMatrix * vtmp).xy;
    // fTouch = vTouch;
}