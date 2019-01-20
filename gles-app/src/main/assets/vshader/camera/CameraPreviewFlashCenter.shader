uniform mat4 vMatrix;

attribute vec4 vPosition;

attribute vec2 vCoord;
varying vec2 textureCoordinate;

varying vec2 fCenter;

void main() {
    gl_Position = vMatrix * vPosition;
    vec4 vCenter = vec4(0.5, 0.5, 0, 0);
    fCenter = vCenter.xy;
    //
    textureCoordinate = vCoord;
}