package de.hsrm.objectify.rendering;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;

public class ObjectModel {
	
	private static final String TAG = "ObjectModel";
	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;
	private ShortBuffer indexBuffer;
	private int[] textures = new int[1];
	private float vertices[];
	private float n_vertices[];
	private short faces[];
	
	public ObjectModel() {
		
	}
	
	public ObjectModel(InputStream is) {
		// TODO Auto-generated constructor stub
	}

	public void draw(GL10 gl) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLES, faces.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	public void loadGLTexture(GL10 gl, Context context) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Scales image to valid texture
	 * @param image original image
	 * @param size preferred width and height size
	 * @return scaled image
	 */
	private Bitmap scaleTexture(Bitmap image, int size) {
		int width = image.getWidth();
		int height = image.getHeight();
		float scaleWidth = ((float) size) / width;
		float scaleHeight = ((float) size) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
	}

}
