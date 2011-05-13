package de.hsrm.objectify.rendering;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import de.hsrm.objectify.utils.ImageHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.opengl.GLUtils;

public class ObjectModel {
	
	private static final String TAG = "ObjectModel";
	private FloatBuffer vertexBuffer;
	private FloatBuffer normalBuffer;
	private ShortBuffer indexBuffer;
	private int[] textures;
	private float vertices[];
	private float n_vertices[];
	private short faces[];
	private byte[] bb;
	private Bitmap image;
	
	public ObjectModel(String path) {
		textures = new int[1];
		vertices = new float[1];
		n_vertices = new float[1];
		faces = new short[1];
		image = BitmapFactory.decodeFile(path);
		setVertexBuffer(vertices);
		setNormalBuffer(n_vertices);
		setFacesBuffer(faces);
	}
	
	public void putVertices(float[] verts) {
		this.vertices = new float[verts.length];
		System.arraycopy(verts, 0, this.vertices, 0, verts.length);
		setVertexBuffer(this.vertices);
	}
	
	public void putNVertices(float[] nverts) {
		this.n_vertices = new float[nverts.length];
		System.arraycopy(nverts, 0, this.n_vertices, 0, nverts.length);
		setNormalBuffer(this.n_vertices);
	}
	
	public void putFaces(short[] face) {
		this.faces = new short[face.length];
		System.arraycopy(face, 0, faces, 0, face.length);
		setFacesBuffer(this.faces);
	}
	
	private void setVertexBuffer(float[] vertices) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		this.vertexBuffer = vbb.asFloatBuffer();
		for (float f : vertices) {
			vertexBuffer.put(f);
		}
		vertexBuffer.rewind();
	}
	
	private void setNormalBuffer(float[] normals) {
		ByteBuffer nbb = ByteBuffer.allocateDirect(normals.length * 4);
		nbb.order(ByteOrder.nativeOrder());
		this.normalBuffer = nbb.asFloatBuffer();
		for (float f : n_vertices) {
			normalBuffer.put(f);
		}
		normalBuffer.rewind();
	}
	
	private void setFacesBuffer(short[] faces) {
		ByteBuffer fbb = ByteBuffer.allocateDirect(faces.length * 2);
		fbb.order(ByteOrder.nativeOrder());
		indexBuffer = fbb.asShortBuffer();
		for (short s : faces) {
			indexBuffer.put(s);
		}
		indexBuffer.rewind();
	}
	
	public void draw(GL10 gl) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glFrontFace(GL10.GL_CCW);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
//		gl.glDrawElements(GL10.GL_TRIANGLES, faces.length, GL10.GL_UNSIGNED_SHORT, indexBuffer);
		// Wireframe
//		gl.glDrawArrays(GL10.GL_LINES, 0, vertices.length/3);
		// normal
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length/3);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	public void loadGLTexture(GL10 gl, Context context) {
		Bitmap texture = scaleTexture(image, 256);
		
		gl.glGenTextures(1, textures, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		
		if (texture != null) {
//			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, texture, 0);
//			// Alternative
//			ByteBuffer bytebuf = ByteBuffer.allocateDirect(texture.getHeight() * texture.getWidth() * 4);
//			bytebuf.order(ByteOrder.nativeOrder());
//			IntBuffer pixelbuf = bytebuf.asIntBuffer();
//			
//			for (int y = 0; y < texture.getHeight(); y++) {
//				for (int x = 0; x < texture.getWidth(); x++) {
//					pixelbuf.put(texture.getPixel(x, y));
//				}
//			}
//			pixelbuf.position(0);
//			bytebuf.position(0);
//			
//			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, texture.getWidth(), texture.getHeight(), 0, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixelbuf);
		}
		
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
