precision mediump float;
// 纹理1
uniform sampler2D vTexture;
varying vec2 textureCoordinate;
// 纹理2
uniform sampler2D vTexture2;
varying vec2 textureCoordinate2;

varying vec2 varyPostion; // vPosition.xy

// 定义左上角矩形融合区域，参考顶点位置坐标定义（顶点位置一般范围[-1，1]）
const vec2 leftBottom = vec2(-1.0, 0.65);
const vec2 rightTop = vec2(-0.7, 1.0);

void main() {
    vec4 base = texture2D(vTexture, textureCoordinate);
    // 1. 灰度
    // float rgb = base.g;
    // vec4 c = vec4(rgb, rgb, rgb, base.a);
    
    // gl_FragColor = vec4(ra, ga, ba, 1.0);
    // gl_FragColor = base;
    // gl_FragColor = overlay;
    // 矩形区域内的融合
    if (varyPostion.x >= leftBottom.x && varyPostion.x <= rightTop.x
     && varyPostion.y >= leftBottom.y && varyPostion.y <= rightTop.y) {
        vec2 tex0 = vec2((varyPostion.x-leftBottom.x) / (rightTop.x-leftBottom.x),
                     1.0 - (varyPostion.y-leftBottom.y) / (rightTop.y-leftBottom.y));
        vec4 overlay = texture2D(vTexture2, tex0);
        // 以overlay的透明度overlay.a 进行 mix.
        // gl_FragColor = overlay * overlay.a + texture2D(vTexture, 1.0-textureCoordinate)*(1.0-overlay.a);
        // 第三个参数为第二个参数在混合运算中所占的比重
        gl_FragColor = mix(base, overlay, 0.4);
    } else { // 矩形区域外的融合
        // vec4 overlay = texture2D(vTexture2, textureCoordinate2);
        // float ra = (base.r + overlay.r) / 2.0;
        // float ga = (base.g + overlay.g) / 2.0;
        // float ba = (base.b + overlay.b) / 2.0;
        // gl_FragColor = mix(overlay, base, 1.0);
        gl_FragColor = base;
    }
}
