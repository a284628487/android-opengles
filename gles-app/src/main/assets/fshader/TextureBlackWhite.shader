precision mediump float;

uniform sampler2D vTexture;
varying vec2 aCoordinate;

vec4 bwColor(vec4 color){
    float r = (color.r > 0.55) ? 1.0 : 0.0;
    float g = (color.g > 0.55) ? 1.0 : 0.0;
    float b = (color.b > 0.55) ? 1.0 : 0.0;
    if(r + g < 2.0) {
        return vec4(0, 0, 0, 0);
    }
    return vec4(1, 1, 1, 0);
}

void main() {
    vec4 originalColor = texture2D(vTexture, aCoordinate);
    gl_FragColor = bwColor(originalColor);
}