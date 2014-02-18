package de.hsrm.objectify.camera;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import de.hsrm.objectify.rendering.Circle;

/**
 * This class implements an opengl es based lighting mode, where an illuminated
 * {@link Circle} can be placed onto the screen.
 * 
 * @author kwolf001
 * 
 */
public class CameraLighting extends GLSurfaceView {

	private CameraLightingRenderer renderer;

	public CameraLighting(Context context) {
		super(context);
		renderer = new CameraLightingRenderer(context);
		setRenderer(renderer);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

	public CameraLighting(Context context, AttributeSet attrs) {
		super(context, attrs);
		renderer = new CameraLightingRenderer(context);
		setRenderer(renderer);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

	/**
	 * Set up the light source depending on the total number of pictures used
	 * and the current picture taken.
	 * 
	 * @param numberOfPictures
	 *            total number of pictures used for 3d reconstruction.
	 */
	public void putLightSource(int numerOfTotalPictures, int currentPictureCount) {
		renderer.putLightSource(numerOfTotalPictures, currentPictureCount);
	}

	private class CameraLightingRenderer implements GLSurfaceView.Renderer {

		private float xcoord = 0.0f;
		private float ycoord = 0.0f;
		private float RADIUS_LIGHTSOURCE = 3.5f;
		private float RADIUS_LIGHTPATH = 4.5f;
		private float ratio;
		private Circle lightSource;
		private boolean toggle = false;

		public CameraLightingRenderer(Context context) {
			this.lightSource = new Circle(RADIUS_LIGHTSOURCE);
		}

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glDepthFunc(GL10.GL_LEQUAL);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_LIGHT0);
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			ratio = (float) width / height;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
			gl.glViewport(0, 0, width, height);
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslatef(xcoord, ycoord, -3.0f);
			lightSource.draw(gl);
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
			if (toggle) {
				toggle = false;
				CameraActivity.handler.post(CameraActivity.shootPicture);
			}
		}

		public void putLightSource(int numberOfTotalPictures,
				int currentPictureCount) {
			float currentDegree = (360 / numberOfTotalPictures)
					* currentPictureCount;
			xcoord = (float) (RADIUS_LIGHTPATH * (Math
					.cos(getAngle(currentDegree))));
			ycoord = (float) (RADIUS_LIGHTPATH * (Math
					.sin(getAngle(currentDegree))));
			toggle = true;
			requestRender();
		}

		private double getAngle(float angle) {
			return (angle * Math.PI) / 180;
		}

	}

}
