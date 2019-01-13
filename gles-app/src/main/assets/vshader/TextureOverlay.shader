uniform mat4 vMatrix;

attribute vec4 vPosition;

attribute vec2 vCoord; // 图片1坐标
varying vec2 textureCoordinate;

attribute vec2 vCoord2; // 图片2坐标
varying vec2 textureCoordinate2;

varying vec2 varyPostion;

void main() {
    gl_Position = vMatrix * vPosition;
    textureCoordinate = vCoord;
    textureCoordinate2 = vCoord2;
    varyPostion = vPosition.xy;
}