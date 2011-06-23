package de.hsrm.objectify.camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.AttributeSet;

public class CameraLighting extends GLSurfaceView {

	private GLU glu;
	private CameraLightingRenderer renderer;
	
	public CameraLighting(Context context) {
		super(context);
		glu = new GLU();
		renderer = new CameraLightingRenderer(context);
		setRenderer(renderer);
	}
	
	public CameraLighting(Context context, AttributeSet attrs) {
		super(context, attrs);
		glu = new GLU();
		renderer = new CameraLightingRenderer(context);
		setRenderer(renderer);
	}

	private class CameraLightingRenderer implements GLSurfaceView.Renderer {

		private Context context;
		private float xcoord = 0;
		private float ycoord = 0;
		private float phi = 0;
		public double radius;
		private FloatBuffer vertices;
		
		public CameraLightingRenderer(Context context) {
			this.context = context;
			ByteBuffer vbb = ByteBuffer.allocateDirect(720 * 4);
			vbb.order(ByteOrder.nativeOrder());
			vertices = vbb.asFloatBuffer();
			for (int i=0; i<720;i+=2) {
				Double x = Math.cos(Math.PI * i / 180);
				Double y = Math.sin(Math.PI * i / 180);
				vertices.put(i, x.floatValue());
				vertices.put(i+1, y.floatValue());
			}
			vertices.rewind();
		}
		
		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			gl.glClearColor(0, 0, 0, 1);
			gl.glClearDepthf(1.0f);
			gl.glEnable(GL10.GL_DEPTH_TEST);
			gl.glDepthFunc(GL10.GL_LEQUAL);
			gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
			gl.glEnable(GL10.GL_NORMALIZE);
			gl.glEnable(GL10.GL_LIGHTING);
			gl.glEnable(GL10.GL_LIGHT0);
		}
		
		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			gl.glViewport(0, 0, width, height);
			
			float ratio = (float) width / height;
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		}
		
		@Override
		public void onDrawFrame(GL10 gl) {
			gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			Double x = 1 + Math.cos(phi);
			Double y = 1 + Math.sin(phi);
			xcoord = x.floatValue();
			ycoord = y.floatValue();
			phi += 0.1;
			
			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadIdentity();
			gl.glTranslatef(xcoord, ycoord, -3.0f);
			gl.glVertexPointer(2, gl.GL_FLOAT, 0, vertices);
			gl.glDrawArrays(gl.GL_TRIANGLE_FAN, 0, 360);
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		}

	}
	
}
