uniform mat4 vMatrix;

attribute vec4 vPosition;

attribute vec4 aColor;
varying vec4 fColor;

void main() {
    gl_Position = vMatrix * vPosition;
    fColor = aColor;
}
