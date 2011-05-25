package de.hsrm.objectify.rendering;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.math.Matrix4f;
import de.hsrm.objectify.math.Quat4f;
import de.hsrm.objectify.utils.ExternalDirectory;

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
	private Context context;
	private int displayWidth, displayHeight;
	private final float TRACKBALL_SCALE_FACTOR = 36.0f;
	private ObjectModelRenderer renderer;
	private ScaleGestureDetector scaleDetector;
	private float skalierung = 1;
	private boolean first = true;
	
	/**
	 * Creates a touchable surface view and ...
	 * @param context
	 * @param objectModel
	 * @param width display width
	 * @param height display height
	 */
	public TouchSurfaceView(Context context, ObjectModel objectModel, int width, int height) {
		super(context);
		this.context = context;
		this.displayWidth = width;
		this.displayHeight = height;
		
		glu = new GLU();
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
		
		public ObjectModelRenderer(Context context, ObjectModel objectModel) {
			this.context = context;
			this.objectModel = objectModel;

			lastRot.setIdentity();
			thisRot.setIdentity();
			thisRot.map(matrix);
		}
		
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glDisable(GL10.GL_DITHER);
//			objectModel.loadGLTexture(gl, this.context);
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

		private void createScreenshot(GL10 gl) {
//			Uri uri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();
//			Cursor c = context.getContentResolver().query(uri, null, selection, selectionArgs, sortOrder)
			int b[] = new int[displayWidth * displayHeight];
			int bt[] = new int[displayWidth * displayHeight];
			IntBuffer ib = IntBuffer.wrap(b);
			ib.position(0);
			gl.glReadPixels(0, 0, displayWidth, displayHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
//			for (int i=0; i<displayHeight; i++) {
//				for (int j=0; j<displayWidth; j++) {
//					// correction of R and B
//					int pix = b[i*displayWidth+j];
//					int pb = (pix >> 16) & 0xFF;
//					int pr = (pix >> 16) & 0x00FF0000;
//					int pixl = (pix & 0xFF00FF00) | pr | pb;
//					// correction of rows
//					bt[(displayHeight-i-1)*displayWidth+j] = pixl;
//				}
//			}
			Bitmap sb = Bitmap.createBitmap(ib.array(), displayWidth, displayHeight, Config.ARGB_8888);
			String path = ExternalDirectory.getExternalImageDirectory() + "/screenshot.png";
			try {
				FileOutputStream fos;
				fos = new FileOutputStream(path);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				sb.compress(CompressFormat.PNG, 80, bos);
				bos.flush();
				bos.close();
			} catch (FileNotFoundException e) {
				Log.e("TouchSurfaceView", e.getMessage());
			} catch (IOException e) {
				Log.e("TouchSurfaceView", e.getMessage());
			}
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
			if (first) {
				first = false;
//				createScreenshot(gl);
			}
				
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
