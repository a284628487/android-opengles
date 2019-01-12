uniform mat4 vMatrix;

attribute vec4 vPosition;

attribute vec2 vCoord;
varying vec2 textureCoordinate;

attribute vec2 vCoord2;
varying vec2 textureCoordinate2;

varying vec2 varyPostion;

void main() {
    gl_Position = vMatrix * vPosition;
    textureCoordinate = vCoord;
    textureCoordinate2 = vCoord2;
    varyPostion = vPosition.xy;
}