attribute vec4 vPosition;

uniform mat4 vMatrix;

varying vec4 fColor;

attribute vec4 vColor;

void main() {
    gl_Position = vMatrix * vPosition;
    fColor = vColor;
}
