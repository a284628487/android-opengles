uniform mat4 vMatrix;

attribute vec4 vPosition;

varying vec4 fColor;

attribute vec4 vColor;

void main() {
    gl_Position = vMatrix * vPosition;
    fColor = vColor;
}
