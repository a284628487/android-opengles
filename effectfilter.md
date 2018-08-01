- BrightnessFilter

```C
uniform sampler2D texture;
uniform float brightness;
varying vec2 v_texcoord;
void main() {
	vec4 color = texture2D(texture, v_texcoord);
	gl_FragColor = brightness * color;
}
```
> brightness = 2;

- BlendFilter

```C
uniform sampler2D texture1;
uniform sampler2D texture2;
uniform float blend;
varying vec2 v_texcoord;
void main() {
	vec4 colorL = texture2D(texture1, v_texcoord);
	vec4 colorR = texture2D(texture2, v_texcoord);
	float weight = colorR.a * blend;
	gl_FragColor = mix(colorL, colorR, weight);
}
```
> blend = 0.5;

- BitmapOverlayFilter

```C
uniform sampler2D tex_sampler_base;
uniform sampler2D tex_sampler_mask;
varying vec2 v_texcoord;
void main() {
	vec4 original = texture2D(tex_sampler_base, v_texcoord);
	vec4 mask = texture2D(tex_sampler_mask, v_texcoord);
	gl_FragColor = vec4(original.rgb * (1.0 - mask.a) + mask.rgb, 1.0);
}
```
