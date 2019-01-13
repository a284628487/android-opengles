uniform mat4 vMatrix;

attribute vec4 vPosition;

attribute vec2 vCoord;
varying vec2 textureCoordinate;

varying vec2 fTouch;
attribute vec2 vTouch;

varying vec2 varyPosition;

void main() {
    gl_Position = vMatrix * vPosition;
    textureCoordinate = vCoord;
    fTouch = vTouch;
    varyPosition = vPosition.xy;
}