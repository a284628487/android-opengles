attribute vec4 vPosition;

varying vec4 fColor;

attribute vec4 vColor;

void main() {
    gl_Position = vPosition;
    fColor = vColor;
}