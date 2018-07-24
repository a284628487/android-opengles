attribute vec4 vPosition;

uniform mat4 vMatrix; // uniform一般用于对同一组顶点组成的3D物体中各个顶点都相同的量

varying vec4 fColor; // varying一般用于从顶点着色器传入到片元着色器的量

attribute vec4 vColor; // attribute一般用于每个顶点都各不相同的量

void main() {
    gl_Position = vMatrix * vPosition;
    fColor = vColor;
}

// 顶点着色器(增加了矩阵变换)