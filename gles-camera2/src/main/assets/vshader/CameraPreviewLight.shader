uniform mat4 vMatrix;

attribute vec4 vPosition;

attribute vec2 vCoord;
varying vec2 textureCoordinate;

void main() {
    gl_Position = vMatrix * vPosition;
    textureCoordinate = vCoord;
}