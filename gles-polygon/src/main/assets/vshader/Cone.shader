uniform mat4 vMatrix;

attribute vec4 vPosition;

varying vec4 fColor;

void main(){
    gl_Position = vMatrix * vPosition;
    if(vPosition.z != 0.0) {
        fColor = vec4(0.9, 0.0, 0.0, 1.0);
    }else{
        fColor = vec4(0.9, 0.9, 0.9, 1.0);
    }
}