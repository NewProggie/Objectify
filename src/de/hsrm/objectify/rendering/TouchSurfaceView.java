package de.hsrm.objectify.rendering;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Calendar;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.math.Matrix4f;
import de.hsrm.objectify.math.Quat4f;
import de.hsrm.objectify.utils.ExternalDirectory;
import de.hsrm.objectify.utils.OBJFormat;

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
	 * @param context
	 * @param objectModel
	 * @param width display width
	 * @param height display height
	 */
	public TouchSurfaceView(Context context, ObjectModel objectModel, int width, int height) {
		super(context);
		this.displayWidth = width;
		this.displayHeight = height;
		
		arcBall.setBounds((float) width, (float) height);
		renderer = new ObjectModelRenderer(context, objectModel);
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
		int x = displayWidth - (Float.valueOf(event.getX()).intValue());
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
	
	public Bitmap getSurfaceBitmap() {
		renderer.shouldCopySurface = true;
		requestRender();
		SystemClock.sleep(200);
		return renderer.getSurfaceBitmap();
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
			// Maximale Skalierung festlegen, damit das Objekt nicht komplett verschwinden kann
			if (skalierung < 0.5f)
				skalierung = 0.5f;
			else if (skalierung > 1.5f)
				skalierung = 1.5f;
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
		private boolean shouldCopySurface = false;
		private Bitmap surfaceBitmap;
		public float angleX, angleY;
		private float angle = 0.0f;
		/**
		 * used for indicating whether it's a newly created object and therefore
		 * needs to be written to database.
		 */
		private boolean onFirstStart = true;
		
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
			gl.glClearColor(0.6f, 0.6f, 0.6f, 0.5f); 
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glDepthFunc(GL10.GL_LEQUAL);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
			gl.glEnable(GL10.GL_NORMALIZE);
			
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_LIGHT0);
			// define ambient component of first light
			float[] light0Ambient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
			ByteBuffer byteBuf = ByteBuffer.allocateDirect(light0Ambient.length * 4);
			byteBuf.order(ByteOrder.nativeOrder());
			FloatBuffer ambientBuffer = byteBuf.asFloatBuffer();
			ambientBuffer.put(light0Ambient);
			ambientBuffer.rewind();
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, ambientBuffer);
			// define diffuse component of first light
			float[] light0Diffuse = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
			FloatBuffer diffuseBuffer = byteBuf.asFloatBuffer();
			diffuseBuffer.put(light0Diffuse);
			diffuseBuffer.rewind();
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, diffuseBuffer);
			// define specular component of first light
			float[] light0Specular = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
			FloatBuffer specularBuffer = byteBuf.asFloatBuffer();
			specularBuffer.put(light0Specular);
			specularBuffer.rewind();
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, specularBuffer);
			float[] light0Position = new float[] { 0.0f, 5.0f, 5.0f, 0.0f };
			FloatBuffer lightPosBuffer = byteBuf.asFloatBuffer();
			lightPosBuffer.put(light0Position);
			lightPosBuffer.rewind();
			gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosBuffer);
			gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 60.0f);

			gl.glMatrixMode(GL10.GL_MODELVIEW);
		}

		/**
		 * Writes the created object in an {@link AsyncTask} into the database.
		 * Happens only once after the first frame was successfully drawn on the
		 * display, so that we have a proper screenshot of the calculated
		 * object.
		 * 
		 * @param gl
		 *            the GL interface
		 */
		private void persist(GL10 gl) {
			// need to copy pixels from gl, before writing into database inside an AsyncTask
			IntBuffer intBuffer = IntBuffer.wrap(new int[displayWidth * displayHeight]);
			intBuffer.position(0);
			gl.glReadPixels(0, 0, displayWidth, displayHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
			new AsyncTask<IntBuffer, Void, Void>() {

				@Override
				protected Void doInBackground(IntBuffer... params) {
					IntBuffer intBuffer = params[0];
					Uri uri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();
					ContentResolver cr = context.getContentResolver();
					ContentValues values = new ContentValues();
					
					Bitmap screenshot = Bitmap.createBitmap(intBuffer.array(), displayWidth, displayHeight, Config.ARGB_8888);
					Bitmap smallScreenshot = Bitmap.createScaledBitmap(screenshot, ((int) displayWidth/8), ((int) displayHeight/8), true);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					smallScreenshot.compress(CompressFormat.PNG, 100, baos);
					values.put(DatabaseAdapter.GALLERY_IMAGE_KEY, baos.toByteArray());
					values.put(DatabaseAdapter.GALLERY_SIZE_KEY, "0");
					values.put(DatabaseAdapter.GALLERY_FACES_KEY, String.valueOf(objectModel.getFaces()).length());
					values.put(DatabaseAdapter.GALLERY_VERTICES_KEY, String.valueOf(objectModel.getVertices()).length());
					values.put(DatabaseAdapter.GALLERY_DIMENSIONS_KEY, String.valueOf(displayWidth)+"x"+String.valueOf(displayHeight));
					values.put(DatabaseAdapter.GALLERY_DATE_KEY, String.valueOf(Calendar.getInstance().getTimeInMillis()));
					cr.insert(uri, values);

					return null;
				}


			}.execute(intBuffer);
		}
		
		@Override
		public void onDrawFrame(GL10 gl) {
			gl.glColor4f(0, 0, 0, 0);
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			// wichtig f�r die Arcball-Rotation
			               // eye    |   center |   up  
			GLU.gluLookAt(gl, 0, 0,-2,   0, 0, 0,   0, 1, 0);
			thisRot.map(matrix);
			gl.glMultMatrixf(matrix, 0);
			gl.glRotatef(180, 1, 0, 0);
			gl.glRotatef(270, 0, 0, 1);
			gl.glScalef(skalierung, skalierung, skalierung);
			gl.glScalef(objectModel.getLength(), objectModel.getLength(), objectModel.getLength());
			gl.glTranslatef(-objectModel.getMiddlePoint()[0], -objectModel.getMiddlePoint()[1], -objectModel.getMiddlePoint()[2]);
			objectModel.draw(gl);
			if (shouldCopySurface) {
				shouldCopySurface = false;
				IntBuffer intBuffer = IntBuffer.wrap(new int[displayWidth * displayHeight]);
				intBuffer.position(0);
				gl.glReadPixels(0, 0, displayWidth, displayHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
				surfaceBitmap = Bitmap.createBitmap(intBuffer.array(), displayWidth, displayHeight, Config.ARGB_8888);
			}
			if (onFirstStart) {
				onFirstStart = false;
				persist(gl);
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
		
	}

}
