package de.hsrm.objectify.rendering;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import de.hsrm.objectify.math.Matrix4f;
import de.hsrm.objectify.math.Quat4f;
import de.hsrm.objectify.utils.ArcBall;

/**
 * Creates a touchable surface view to move, scale and spin a rendered object on
 * the display screen. This class extends {@link GLSurfaceView} and makes use of
 * {@link ScaleGestureDetector} which was introduced with Android version 2.2
 * (Froyo).
 *
 * @author kwolf001
 */
public class TouchSurfaceView extends GLSurfaceView {

    private static final String TAG = "TouchSurfaceView";
    private final Object matrixLock = new Object();
    private final float TRACKBALL_SCALE_FACTOR = 36.0f;
    private Matrix4f lastRot = new Matrix4f();
    private Matrix4f thisRot = new Matrix4f();
    private float[] matrix = new float[16];
    private ArcBall arcBall = new ArcBall(getWidth(), getHeight());
    private int displayWidth, displayHeight;
    private ObjectModelRenderer renderer;
    private ScaleGestureDetector scaleDetector;
    private float mScaling = 1;

    public TouchSurfaceView(Context context, int width, int height) {
        super(context);
        this.displayWidth = width;
        this.displayHeight = height;

        arcBall.setBounds((float) width, (float) height);
        renderer = new ObjectModelRenderer();
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        scaleDetector = new ScaleGestureDetector(context,
                new SimpleScaleListener());
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        renderer.mAngleX += event.getX() * TRACKBALL_SCALE_FACTOR;
        renderer.mAngleY += event.getY() * TRACKBALL_SCALE_FACTOR;
        requestRender();
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getPointerCount() > 1) {
            scaleDetector.onTouchEvent(event);
            requestRender();
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                synchronized (matrixLock) {
                    lastRot.copy(thisRot);
                }
                arcBall.click(new PointF(x, y));
                break;
            case MotionEvent.ACTION_MOVE:
                Quat4f thisQuat = new Quat4f();
                arcBall.drag(new PointF(x, y), thisQuat);
                synchronized (matrixLock) {
                    thisRot.setRotation(thisQuat);
                    thisRot = Matrix4f.mul(lastRot, thisRot);
                }
                requestRender();
                break;
        }
        return true;
    }

    public void setObjectModel(ObjectModel objectModel) {
        renderer.setObjectModel(objectModel);
    }

    private class SimpleScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        public boolean onScale(ScaleGestureDetector detector) {
            mScaling *= detector.getScaleFactor();
            if (mScaling < 0.5f) mScaling = 0.5f;
            if (mScaling > 100.5f) mScaling = 100.5f;
            invalidate();
            return true;
        }
    }

    /**
     * This class takes care of the rendering of the model, implementing
     * {@link GLSurfaceView.Renderer} and adapting for our needs.
     *
     * @author kwolf001
     */
    private class ObjectModelRenderer implements GLSurfaceView.Renderer {

        public float mAngleX;
        public float mAngleY;
        private ObjectModel mObjectModel = null;

        public ObjectModelRenderer() {
            lastRot.setIdentity();
            thisRot.setIdentity();
            thisRot.map(matrix);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glShadeModel(GL10.GL_SMOOTH);
            gl.glClearColor(0.2f, 0.3f, 0.5f, 1.0f);
            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
            gl.glEnable(GL10.GL_NORMALIZE);

            gl.glEnable(GL10.GL_LIGHTING);
            gl.glEnable(GL10.GL_LIGHT0);
            // LIGHT0
            // define ambient component of first light
            float[] light0Ambient = new float[]{0.9f, 0.9f, 0.9f, 1.0f};
            ByteBuffer byteBuf = ByteBuffer.allocateDirect(light0Ambient.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            FloatBuffer light0AmbientBuffer = byteBuf.asFloatBuffer();
            light0AmbientBuffer.put(light0Ambient);
            light0AmbientBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light0AmbientBuffer);
            // define diffuse component of first light
            float[] light0Diffuse = new float[]{0.8f, 0.8f, 0.8f, 1.0f};
            FloatBuffer light0diffuseBuffer = byteBuf.asFloatBuffer();
            light0diffuseBuffer.put(light0Diffuse);
            light0diffuseBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light0diffuseBuffer);
            // define specular component of first light
            float[] light0Specular = new float[]{0.8f, 0.8f, 0.8f, 1.0f};
            FloatBuffer light0specularBuffer = byteBuf.asFloatBuffer();
            light0specularBuffer.put(light0Specular);
            light0specularBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, light0specularBuffer);
            float[] light0Position = new float[]{1.0f, 3.0f, 3.0f, 1.0f};
            FloatBuffer lightPosBuffer = byteBuf.asFloatBuffer();
            lightPosBuffer.put(light0Position);
            lightPosBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosBuffer);
            gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 65.0f);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
        }

        @Override
        public void onDrawFrame(GL10 gl) {

            gl.glColor4f(0, 0, 0, 0);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            GLU.gluLookAt(gl, 0, 0, -2, 0, 0, 0, 0, 1, 0);
            thisRot.map(matrix);
            gl.glMultMatrixf(matrix, 0);
            gl.glScalef(mScaling, mScaling, mScaling);

            if (mObjectModel != null) {
                gl.glScalef(mObjectModel.getLength(),
                        mObjectModel.getLength(),
                        mObjectModel.getLength());
                gl.glTranslatef(-mObjectModel.getMiddlePoint()[0],
                        -mObjectModel.getMiddlePoint()[1],
                        -mObjectModel.getMiddlePoint()[2]);
                mObjectModel.draw(gl);
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
            gl.glViewport(0, 0, width, height);
        }

        public void setObjectModel(ObjectModel objectModel) {
            mObjectModel = objectModel;
            requestRender();
        }

    }

}