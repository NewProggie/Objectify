package de.hsrm.objectify.camera;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import de.hsrm.objectify.math.Matrix;
import de.hsrm.objectify.rendering.Circle;

/**
 * This class implements an opengl es based lighting mode, where an illuminated {@link Circle} can be placed onto the screen.
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
			lightMatrix[0][0] = 0.01693;
			lightMatrix[0][1] = 0.16890;
			lightMatrix[0][2] = 1.01483;
			lightMatrix[1][0] = 0.01042;
			lightMatrix[1][1] = 0.06380;
			lightMatrix[1][2] = 1.00230;
			lightMatrix[2][0] = 0.07943;
			lightMatrix[2][1] = 0.17188;
			lightMatrix[2][2] = 1.01186;
			lightMatrix[3][0] = 0.04167;
			lightMatrix[3][1] = 0.23307;
			lightMatrix[3][2] = 1.02640;
		} else if (numberOfPictures == 5) {
			lightMatrix[0][0] = 0.03966;
			lightMatrix[0][1] = 0.16890;
			lightMatrix[0][2] = 1.01483;
			lightMatrix[1][0] = 0.02270;
			lightMatrix[1][1] = 0.10867;
			lightMatrix[1][2] = 1.00593;
			lightMatrix[2][0] = 0.06534;
			lightMatrix[2][1] = 0.10649;
			lightMatrix[2][2] = 1.00352;
			lightMatrix[3][0] = 0.07614;
			lightMatrix[3][1] = 0.23343;
			lightMatrix[3][2] = 1.02421;
			lightMatrix[4][0] = 0.03922;
			lightMatrix[4][1] = 0.24588;
			lightMatrix[4][2] = 1.02916;
		} else if (numberOfPictures == 6) {
			lightMatrix[0][0] = 0.01061;
			lightMatrix[0][1] = 0.20498;
			lightMatrix[0][2] = 1.02107;
			lightMatrix[1][0] = -0.00643;
			lightMatrix[1][1] = 0.10405;
			lightMatrix[1][2] = 1.00542;
			lightMatrix[2][0] = 0.03613;
			lightMatrix[2][1] = 0.10528;
			lightMatrix[2][2] = 1.00483;
			lightMatrix[3][0] = 0.06335;
			lightMatrix[3][1] = 0.16418;
			lightMatrix[3][2] = 1.01173;
			lightMatrix[4][0] = 0.03651;
			lightMatrix[4][1] = 0.21506;
			lightMatrix[4][2] = 1.02229;
			lightMatrix[5][0] = 0.00412;
			lightMatrix[5][1] = 0.21742;
			lightMatrix[5][2] = 1.02350;
		} else if (numberOfPictures == 7) {
			lightMatrix[0][0] = 0.03921;
			lightMatrix[0][1] = 0.17911;
			lightMatrix[0][2] = 1.01557;
			lightMatrix[1][0] = 0.00862;
			lightMatrix[1][1] = 0.12259;
			lightMatrix[1][2] = 1.00810;
			lightMatrix[2][0] = 0.04275;
			lightMatrix[2][1] = 0.10241;
			lightMatrix[2][2] = 1.00469;
			lightMatrix[3][0] = 0.08475;
			lightMatrix[3][1] = 0.13522;
			lightMatrix[3][2] = 1.00592;
			lightMatrix[4][0] = 0.08690;
			lightMatrix[4][1] = 0.18026;
			lightMatrix[4][2] = 1.01263;
			lightMatrix[5][0] = 0.06341;
			lightMatrix[5][1] = 0.21369;
			lightMatrix[5][2] = 1.02053;
			lightMatrix[6][0] = 0.02536;
			lightMatrix[6][1] = 0.19528;
			lightMatrix[6][2] = 1.01861;
		} else if (numberOfPictures == 8) {
			lightMatrix[0][0] = -0.03815;
			lightMatrix[0][1] = 0.14344;
			lightMatrix[0][2] = 1.00704;
			lightMatrix[1][0] = -0.03014;
			lightMatrix[1][1] = 0.11026;
			lightMatrix[1][2] = 1.00484;
			lightMatrix[2][0] = -0.01174;
			lightMatrix[2][1] = 0.09901;
			lightMatrix[2][2] = 1.00249;
			lightMatrix[3][0] = 0.00917;
			lightMatrix[3][1] = 0.11213;
			lightMatrix[3][2] = 1.00084;
			lightMatrix[4][0] = 0.00089;
			lightMatrix[4][1] = 0.13788;
			lightMatrix[4][2] = 1.00413;
			lightMatrix[5][0] = -0.02036;
			lightMatrix[5][1] = 0.16001;
			lightMatrix[5][2] = 1.00670;
			lightMatrix[6][0] = -0.03744;
			lightMatrix[6][1] = 0.17476;
			lightMatrix[6][2] = 1.00917;
			lightMatrix[7][0] = -0.05669;
			lightMatrix[7][1] = 0.14003;
			lightMatrix[7][2] = 1.00493;
		} else if (numberOfPictures == 9) {
			lightMatrix[0][0] = 0.03738;
			lightMatrix[0][1] = 0.19945;
			lightMatrix[0][2] = 1.01924;
			lightMatrix[1][0] = -0.00034;
			lightMatrix[1][1] = 0.13173;
			lightMatrix[1][2] = 1.00878;
			lightMatrix[2][0] = 0.03120;
			lightMatrix[2][1] = 0.11857;
			lightMatrix[2][2] = 1.00661;
			lightMatrix[3][0] = 0.04811;
			lightMatrix[3][1] = 0.11034;
			lightMatrix[3][2] = 1.00489;
			lightMatrix[4][0] = 0.06716;
			lightMatrix[4][1] = 0.13489;
			lightMatrix[4][2] = 1.00692;
			lightMatrix[5][0] = 0.05521;
			lightMatrix[5][1] = 0.17919;
			lightMatrix[5][2] = 1.001427;
			lightMatrix[6][0] = 0.04823;
			lightMatrix[6][1] = 0.19635;
			lightMatrix[6][2] = 1.01777;
			lightMatrix[7][0] = 0.03646;
			lightMatrix[7][1] = 0.20155;
			lightMatrix[7][2] = 1.01930;
			lightMatrix[8][0] = 0.01472;
			lightMatrix[8][1] = 0.20436;
			lightMatrix[8][2] = 1.02055;
		}
		return new Matrix(lightMatrix);
	}

	private class CameraLightingRenderer implements GLSurfaceView.Renderer {

		private Context context;
		private float xcoord = 0.0f;
		private float ycoord = 0.0f;
		private float RADIUS = 2.7f;
		private int width, height;
		private float ratio;
		private Circle lightSource;
		
		public CameraLightingRenderer(Context context) {
			this.context = context;
			this.lightSource = new Circle(RADIUS);
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
			this.width = width;
			this.height = height;
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
		}
		
		public void putLightSource(int numberOfTotalPictures, int currentPictureCount) {
			float currentDegree = (360/numberOfTotalPictures)*currentPictureCount;
			xcoord = (float) (3.0f * (Math.cos(getAngle(currentDegree))));
			ycoord = (float) (3.0f * (Math.sin(getAngle(currentDegree))));
			requestRender();
		}
		
		private double getAngle(float angle) {
			return (angle*Math.PI)/180;
		}

	}
	
}
