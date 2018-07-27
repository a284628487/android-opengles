
## GLSurfaceView

**SurfaceView**的子类，使用它的`Surface`来进行`OpenGL`渲染。
它提供了以下几个特性
- Manages a surface, which is a special piece of memory that can be composited into the Android view system.
    管理Surface，它是一段特殊的内存区域。
- Manages an EGL display, which enables OpenGL to render into a surface.
    管理EGL显示，将opengl渲染到surface。
- Accepts a user-provided Renderer object that does the actual rendering.
    接收用户提供的真实的渲染内容。
- Renders on a dedicated thread to decouple rendering performance from the UI thread.
    在特定的线程(内部有一个渲染线程**GLThread**)进行渲染，和UI thread解耦。
- Supports both on-demand and continuous rendering.
    支持条件渲染以及一直不停渲染两种模式。


### Start

通常使用`GLSurfaceView`，都是继承自它并且根据需要重写它的方法，比如对用户的输入做特殊的处理。
如果我们的应用不需要响应输入事件，则直接使用它。和普通的View不同的是，绘制(drawing)操作是在一个分离出来的叫`Renderer`的对象中进行，而不是`onDraw`。
通常直接调用`setRenderer`就行了，但是，可以在`setRenderer()`之前通过一些方法修改`GLSurfaceView`的一些行为。

- setDebugFlags(int)
- setEGLConfigChooser(boolean)
- setEGLConfigChooser(EGLConfigChooser)
- setEGLConfigChooser(int, int, int, int, int, int)
- setGLWrapper(GLWrapper)

`GLSurfaceView`默认创建的`Surface`是**PixelFormat.RGB_888**格式的，如果需要使用带透明度的Surface，可以使用
`getHolder().setFormat(PixelFormat.TRANSLUCENT)`实现。

### Debug

在`setRenderer`之前调用`setDebugFlags()`和`setGLWrapper()`。

### Life-cycle

当`Activity` pause 和 resume 的时候，也需要调用GLSurfaceView的生命周期方法`onPause`和`onResume()`。
这两个操作会使 `GLSurfaceView` pause / resume 渲染线程。

### Handling events

可以通过`queueEvent(Runnable run)`提交一个`Runnable`和`Renderer`直接交互，这个`Runnable`在每次`onDrawFrame`之前被调用。

```Java
class MyGLSurfaceView extends GLSurfaceView {

    private MyRenderer mMyRenderer;

    public void start() {
        mMyRenderer = ...;
        setRenderer(mMyRenderer);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            queueEvent(new Runnable() {
                // This method will be called on the rendering
                // thread:
                public void run() {
                    mMyRenderer.handleDpadCenter();
                }});
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
```