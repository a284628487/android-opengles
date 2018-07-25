## 顶点着色器 Vertex Shader

### Special Output Variables(RW)

```C
vec4 gl_Position; // 投影坐标，在shader中必须指定。
float gl_PointSize; // enable GL_VERTEX_PROGRAM_POINT_SIZE
vec4 gl_ClipVertex;
```

## 片段着色器 Fragment Shader

### Special Output Variables(RW)

```C
vec4 gl_FragColor; // 像素点颜色，可以是颜色值，也可以是纹理颜色。
vec4 gl_FragData[gl_MaxDrawBuffers];
float gl_FragDepth; DEFAULT=glFragCoord.z
```

## Data type

```C
float, vec2, vec3, vec4
int, ivec2, ivec3, ivec4
bool, bvec2, bvec3, bvec4
mat2, mat3, mat4
void
sampler1D, sampler2D, sampler3D
samplerCube
sampler1DShadow, sampler2DShadow
samplerExternalOES 
```

## Data type qualifires

### global variable declarations:
- uniform: 适用于 Vertex/Fragment shader，通常用于对于所有像素点都一样的变量比如坐标投影。(READ-ONLY)
- attribute: 适用于 Vertext shader，通常用于设置每个顶点的参数数值如坐标，颜色。(READ-ONLY)
- varying: 从 Vertex shader 中经过处理(READ/WRITE), 输出到 Fragment shader (READ-ONLY)
- const: compile-time constant (READ-ONLY)

### function parameters:
- in: value initialized on entry, not copied on return (default)
- out: copied out on return, but not initialized
- inout: value initialized on entry, and cpoied out on return
- const: constant function input

## Vector Components

vec4:
- `x, y, z, w` // 坐标
- `r, g, b, a` // 颜色值
- `s, t, p, q` // 纹理
