package de.hsrm.objectify.rendering;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import de.hsrm.objectify.math.Matrix4f;
import de.hsrm.objectify.math.Quat4f;

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
	private GLU glu;
	private int width, height;
	private final float TRACKBALL_SCALE_FACTOR = 36.0f;
	private ObjectModelRenderer renderer;
	private ScaleGestureDetector scaleDetector;
	private float skalierung = 1;
	
	public TouchSurfaceView(Context context, String path, int width, int height) {
		super(context);
		this.width = width;
		this.height = height;
		
		glu = new GLU();
		arcBall.setBounds((float) width, (float) height);
		renderer = new ObjectModelRenderer(context, path);
		setRenderer(renderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		scaleDetector = new ScaleGestureDetector(context, new SimpleScaleListener());
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
		int x = width - (Float.valueOf(event.getX()).intValue());
		int y = Float.valueOf(event.getY()).intValue();
		scaleDetector.onTouchEvent(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			synchronized (matrixLock) {
				lastRot.copy(thisRot);
			}
			arcBall.click(new Point(x, y));
			break;
		case MotionEvent.ACTION_MOVE:
			Quat4f thisQuat = new Quat4f();
			arcBall.drag(new Point(x, y), thisQuat);
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
	private class SimpleScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		
		public boolean onScale(ScaleGestureDetector detector) {
			skalierung *= detector.getScaleFactor();
			invalidate();
			return true;
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
		public float angleX, angleY;
		
		public ObjectModelRenderer(Context context, String path) {
			this.context = context;
			objectModel = new ObjectModel(path);
			float[] vertices = new float[] 
			                             { -1.0f, -1.0f, 0.0f,
											1.0f, -1.0f, 0.0f, 
											-1.0f, 1.0f, 0.0f, 	
											1.0f, 1.0f, 0.0f };
			objectModel.putVertices(vertices);
			lastRot.setIdentity();
			thisRot.setIdentity();
			thisRot.map(matrix);
		}
		
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glDisable(GL10.GL_DITHER);
			objectModel.loadGLTexture(gl, this.context);
			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glShadeModel(GL10.GL_SMOOTH);
			gl.glClearColor(0.0f, 0.0f, 0.3f, 0.5f); // Black, blue Background
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glDepthFunc(GL10.GL_LEQUAL);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
			gl.glEnable(GL10.GL_NORMALIZE);
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_LIGHT0);
		}
		
		@Override
		public void onDrawFrame(GL10 gl) {
			thisRot.map(matrix);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			glu.gluLookAt(gl, 0, 0, -3, 0, 0, 0, 0, 1, 0);
			
			gl.glPushMatrix();
			gl.glMultMatrixf(matrix, 0);
			gl.glScalef(skalierung, skalierung, skalierung);
			objectModel.draw(gl);
			gl.glPopMatrix();
		}
		
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			gl.glViewport(0, 0, width, height);
			
			float ratio = (float) width / height;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		}
	}

}
