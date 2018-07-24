uniform mat4 vMatrix;

attribute vec4 vPosition;
attribute vec2 vCoordinate;

varying vec2 aCoordinate;
varying vec4 gPosition;

void main(){
    gl_Position = vMatrix * vPosition;
    gPosition = vPosition;
    aCoordinate = vCoordinate;
}