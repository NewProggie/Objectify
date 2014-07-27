package de.hsrm.objectify.rendering;

import android.content.Context;
import android.graphics.Bitmap;
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
import de.hsrm.objectify.utils.BitmapUtils;

/**
 * Creates a touchable surface view to move, scale and spin a rendered object on
 * the display screen. This class extends {@link GLSurfaceView} and makes use of
 * {@link ScaleGestureDetector} which was introduced with Android version 2.2
 * (Froyo).
 *
 * @author kwolf001
 *
 */
public class TouchSurfaceView extends GLSurfaceView {

    private static final String TAG = "TouchSurfaceView";
    private Matrix4f lastRot = new Matrix4f();
    private Matrix4f thisRot = new Matrix4f();
    private float[] matrix = new float[16];
    private final Object matrixLock = new Object();
    private ArcBall arcBall = new ArcBall(getWidth(), getHeight());
    private int displayWidth, displayHeight;
    private final float TRACKBALL_SCALE_FACTOR = 36.0f;
    private ObjectModelRenderer renderer;
    private ScaleGestureDetector scaleDetector;
    private float skalierung = 1;

    /**
     * Creates a touchable surface view and ...
     *
     * @param context
     * @param objectModel
     * @param width
     *            display width
     * @param height
     *            display height
     */
    public TouchSurfaceView(Context context, ObjectModel objectModel,
                            int width, int height) {
        super(context);
        this.displayWidth = width;
        this.displayHeight = height;

        arcBall.setBounds((float) width, (float) height);
//        renderer = new ObjectModelRenderer(context, objectModel);
//        setRenderer(renderer);
        setRenderer(new ClearRenderer());
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        scaleDetector = new ScaleGestureDetector(context,
                new SimpleScaleListener());
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        renderer.angleX += event.getX() * TRACKBALL_SCALE_FACTOR;
        renderer.angleY += event.getY() * TRACKBALL_SCALE_FACTOR;
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

    /**
     * Pinch-and-Zoom implementation. New since Android 2.2 Froyo
     *
     * @author kwolf001
     *
     */
    private class SimpleScaleListener extends
            ScaleGestureDetector.SimpleOnScaleGestureListener {

        public boolean onScale(ScaleGestureDetector detector) {
            skalierung *= detector.getScaleFactor();
            // Maximale Skalierung festlegen, damit das Objekt nicht komplett
            // verschwinden kann
            if (skalierung < 0.5f)
                skalierung = 0.5f;
            else if (skalierung > 1.5f)
                skalierung = 1.5f;
            invalidate();
            return true;
        }
    }

    class ClearRenderer implements GLSurfaceView.Renderer {
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // Do nothing special.
        }

        public void onSurfaceChanged(GL10 gl, int w, int h) {
            gl.glViewport(0, 0, w, h);
        }

        public void onDrawFrame(GL10 gl) {
            gl.glClearColor(0.3f, 0.9f, 0.1f, 1.0f);
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        }
    }

    /**
     * This class takes care of the rendering of the model, implementing
     * {@link GLSurfaceView.Renderer} and adapting for our needs.
     *
     * @author kwolf001
     *
     */
    private class ObjectModelRenderer implements GLSurfaceView.Renderer {

        private ObjectModel objectModel;
        private Context context;
        private boolean shouldCopySurface = false;
        private Bitmap surfaceBitmap;
        public float angleX, angleY;

        public ObjectModelRenderer(Context context, ObjectModel objectModel) {
            this.context = context;
            this.objectModel = objectModel;

            lastRot.setIdentity();
            thisRot.setIdentity();
            thisRot.map(matrix);
        }

        public Bitmap getSurfaceBitmap() {
            return surfaceBitmap;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            objectModel.loadGLTexture(gl, context);
            gl.glEnable(GL10.GL_TEXTURE_2D);
            gl.glShadeModel(GL10.GL_SMOOTH);
            gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
            gl.glClearDepthf(1.0f);
            gl.glEnable(GL10.GL_DEPTH_TEST);
            gl.glDepthFunc(GL10.GL_LEQUAL);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
            gl.glEnable(GL10.GL_NORMALIZE);

            gl.glEnable(GL10.GL_LIGHTING);
            gl.glEnable(GL10.GL_LIGHT0);
            gl.glEnable(GL10.GL_LIGHT1);
            // LIGHT0
            // define ambient component of first light
            float[] light0Ambient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
            ByteBuffer byteBuf = ByteBuffer
                    .allocateDirect(light0Ambient.length * 4);
            byteBuf.order(ByteOrder.nativeOrder());
            FloatBuffer light0AmbientBuffer = byteBuf.asFloatBuffer();
            light0AmbientBuffer.put(light0Ambient);
            light0AmbientBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, light0AmbientBuffer);
            // define diffuse component of first light
            float[] light0Diffuse = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
            FloatBuffer light0diffuseBuffer = byteBuf.asFloatBuffer();
            light0diffuseBuffer.put(light0Diffuse);
            light0diffuseBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, light0diffuseBuffer);
            // define specular component of first light
            float[] light0Specular = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
            FloatBuffer light0specularBuffer = byteBuf.asFloatBuffer();
            light0specularBuffer.put(light0Specular);
            light0specularBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, light0specularBuffer);
            float[] light0Position = new float[] { 0.0f, 5.0f, 5.0f, 1.0f };
            FloatBuffer lightPosBuffer = byteBuf.asFloatBuffer();
            lightPosBuffer.put(light0Position);
            lightPosBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosBuffer);
            gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 65.0f);
            // LIGHT1
            // ambient component
            float[] light1Ambient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
            FloatBuffer light1AmbientBuffer = byteBuf.asFloatBuffer();
            light1AmbientBuffer.put(light1Ambient);
            light1AmbientBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, light1AmbientBuffer);
            // diffuse component
            float[] light1Diffuse = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
            FloatBuffer light1diffuseBuffer = byteBuf.asFloatBuffer();
            light1diffuseBuffer.put(light1Diffuse);
            light1diffuseBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, light1diffuseBuffer);
            // specular component
            float[] light1Specular = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
            FloatBuffer light1specularBuffer = byteBuf.asFloatBuffer();
            light1specularBuffer.put(light1Specular);
            light1specularBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_SPECULAR, light1specularBuffer);
            float[] light1Position = new float[] { 5.0f, 0.0f, 5.0f, 1.0f };
            FloatBuffer light1PosBuffer = byteBuf.asFloatBuffer();
            light1PosBuffer.put(light1Position);
            light1PosBuffer.rewind();
            gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, light1PosBuffer);
            gl.glLightf(GL10.GL_LIGHT1, GL10.GL_SPOT_CUTOFF, 65.0f);

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
            gl.glRotatef(180, 1, 0, 0);
            gl.glRotatef(270, 0, 0, 1);
            gl.glScalef(skalierung, skalierung, skalierung);
            gl.glScalef(objectModel.getLength(), objectModel.getLength(),
                    objectModel.getLength());
            gl.glTranslatef(-objectModel.getMiddlePoint()[0],
                            -objectModel.getMiddlePoint()[1],
                            -objectModel.getMiddlePoint()[2]);
            objectModel.draw(gl);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            float ratio = (float) width / height;
            gl.glMatrixMode(GL10.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
            gl.glViewport(0, 0, width, height);
        }

    }

}