package de.hsrm.objectify.camera;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import de.hsrm.objectify.math.Matrix;
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

	/**
	 * Returns the light matrix (S matrix) depending on the total number of
	 * pictures used for 3d reconstruction
	 * 
	 * @param numberOfPictures
	 *            total number of pictures used for 3d reconstruction
	 * @return the light matrix as a 2d double array
	 */
	public Matrix getLightMatrixS(int numberOfPictures) {
		double[][] lightMatrix = new double[numberOfPictures][3];
		if (numberOfPictures == 4) {
			lightMatrix[0][0] = -0.03365;
			lightMatrix[0][1] = 0.11473;
			lightMatrix[0][2] = 0.99269;
			lightMatrix[1][0] = 0.01680;
			lightMatrix[1][1] = 0.06232;
			lightMatrix[1][2] = 0.99782;
			lightMatrix[2][0] = 0.08802;
			lightMatrix[2][1] = 0.17509;
			lightMatrix[2][2] = 0.98052;
			lightMatrix[3][0] = 0.01382;
			lightMatrix[3][1] = 0.23145;
			lightMatrix[3][2] = 0.97262;
		} else if (numberOfPictures == 5) {
			lightMatrix[0][0] = -0.03960;
			lightMatrix[0][1] = 0.11386;
			lightMatrix[0][2] = 0.99241;
			lightMatrix[1][0] = 0.01238;
			lightMatrix[1][1] = 0.05569;
			lightMatrix[1][2] = 0.99817;
			lightMatrix[2][0] = 0.02847;
			lightMatrix[2][1] = 0.05941;
			lightMatrix[2][2] = 0.99759;
			lightMatrix[3][0] = 0.05322;
			lightMatrix[3][1] = 0.20792;
			lightMatrix[3][2] = 0.97650;
			lightMatrix[4][0] = -0.00619;
			lightMatrix[4][1] = 0.23391;
			lightMatrix[4][2] = 0.97195;
		} else if (numberOfPictures == 6) {
			lightMatrix[0][0] = -0.03342;
			lightMatrix[0][1] = 0.14109;
			lightMatrix[0][2] = 0.98932;
			lightMatrix[1][0] = 0.00866;
			lightMatrix[1][1] = 0.08168;
			lightMatrix[1][2] = 0.99656;
			lightMatrix[2][0] = 0.03465;
			lightMatrix[2][1] = 0.06188;
			lightMatrix[2][2] = 0.99736;
			lightMatrix[3][0] = 0.09530;
			lightMatrix[3][1] = 0.18317;
			lightMatrix[3][2] = 0.97844;
			lightMatrix[4][0] = 0.02351;
			lightMatrix[4][1] = 0.23267;
			lightMatrix[4][2] = 0.97189;
			lightMatrix[5][0] = 0.00371;
			lightMatrix[5][1] = 0.23762;
			lightMatrix[5][2] = 0.97126;
		} else if (numberOfPictures == 7) {
			lightMatrix[0][0] = -0.03218;
			lightMatrix[0][1] = 0.13366;
			lightMatrix[0][2] = 0.99026;
			lightMatrix[1][0] = 0.01609;
			lightMatrix[1][1] = 0.06931;
			lightMatrix[1][2] = 0.99708;
			lightMatrix[2][0] = 0.03342;
			lightMatrix[2][1] = 0.06436;
			lightMatrix[2][2] = 0.99720;
			lightMatrix[3][0] = 0.04455;
			lightMatrix[3][1] = 0.06559;
			lightMatrix[3][2] = 0.99670;
			lightMatrix[4][0] = 0.08292;
			lightMatrix[4][1] = 0.19926;
			lightMatrix[4][2] = 0.97633;
			lightMatrix[5][0] = 0.02723;
			lightMatrix[5][1] = 0.23391;
			lightMatrix[5][2] = 0.97177;
			lightMatrix[6][0] = 0.00495;
			lightMatrix[6][1] = 0.23762;
			lightMatrix[6][2] = 0.97114;
		} else if (numberOfPictures == 8) {
			lightMatrix[0][0] = -0.04455;
			lightMatrix[0][1] = 0.12871;
			lightMatrix[0][2] = 0.99053;
			lightMatrix[1][0] = -0.01361;
			lightMatrix[1][1] = 0.07673;
			lightMatrix[1][2] = 0.99664;
			lightMatrix[2][0] = 0.01609;
			lightMatrix[2][1] = 0.06683;
			lightMatrix[2][2] = 0.99748;
			lightMatrix[3][0] = 0.02970;
			lightMatrix[3][1] = 0.05817;
			lightMatrix[3][2] = 0.99779;
			lightMatrix[4][0] = 0.08911;
			lightMatrix[4][1] = 0.18317;
			lightMatrix[4][2] = 0.97897;
			lightMatrix[5][0] = 0.04084;
			lightMatrix[5][1] = 0.22649;
			lightMatrix[5][2] = 0.97295;
			lightMatrix[6][0] = 0.00124;
			lightMatrix[6][1] = 0.24257;
			lightMatrix[6][2] = 0.97007;
			lightMatrix[7][0] = -0.00619;
			lightMatrix[7][1] = 0.23886;
			lightMatrix[7][2] = 0.97089;
		} else if (numberOfPictures == 9) {
			lightMatrix[0][0] = -0.03713;
			lightMatrix[0][1] = 0.14109;
			lightMatrix[0][2] = 0.98916;
			lightMatrix[1][0] = -0.00248;
			lightMatrix[1][1] = 0.08663;
			lightMatrix[1][2] = 0.99616;
			lightMatrix[2][0] = 0.02228;
			lightMatrix[2][1] = 0.06064;
			lightMatrix[2][2] = 0.99779;
			lightMatrix[3][0] = 0.02847;
			lightMatrix[3][1] = 0.05941;
			lightMatrix[3][2] = 0.99773;
			lightMatrix[4][0] = 0.03589;
			lightMatrix[4][1] = 0.07797;
			lightMatrix[4][2] = 0.99622;
			lightMatrix[5][0] = 0.07673;
			lightMatrix[5][1] = 0.20421;
			lightMatrix[5][2] = 0.97586;
			lightMatrix[6][0] = 0.02104;
			lightMatrix[6][1] = 0.23762;
			lightMatrix[6][2] = 0.97102;
			lightMatrix[7][0] = 0.00990;
			lightMatrix[7][1] = 0.24257;
			lightMatrix[7][2] = 0.97001;
			lightMatrix[8][0] = -0.00990;
			lightMatrix[8][1] = 0.23886;
			lightMatrix[8][2] = 0.97092;
		}
		return new Matrix(lightMatrix);
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
