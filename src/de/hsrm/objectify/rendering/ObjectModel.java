package de.hsrm.objectify.rendering;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import de.hsrm.objectify.R;
import de.hsrm.objectify.utils.ExternalDirectory;

/**
 * A representation of an actual object. Vertices, normals and texture can be
 * added after an instance of this class has been made. This class implements
 * both {@link Parcelable} for sending between different Activities and
 * {@link Serializable} for storing onto the external storage.
 * 
 * @author kwolf001
 * 
 */
public class ObjectModel implements Parcelable {

	private static final String TAG = "ObjectModel";
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	private FloatBuffer normalBuffer;
	private ShortBuffer indexBuffer;
	private float[] texture;
	private int[] textures = new int[1];
	private float vertices[];
	private float n_vertices[];
	private short indices[];
	private byte[] bb;
	private Bitmap image;
	private String image_suffix;
	
	public ObjectModel(float[] vertices, float[] n_vertices, short[] indices, Bitmap image, String image_suffix) {
		setVertices(vertices);
		setNormalVertices(n_vertices);
		setFaces(indices);
		this.image_suffix = image_suffix;
		this.image = Bitmap.createBitmap(image);
	}
	
	private ObjectModel(Parcel source) {
		Bundle b = source.readBundle();
		textures = new int[1];
		setVertices(b.getFloatArray("vertices"));
		n_vertices = new float[1];
		indices = new short[1];
		this.image_suffix = b.getString("image_suffix");
		String path = ExternalDirectory.getExternalImageDirectory()+"/"+this.image_suffix+"_1.png";
		this.image = BitmapFactory.decodeFile(path);
		setVertexBuffer(vertices);
		setNormalBuffer(n_vertices);
		setFacesBuffer(indices);
	}

	public void setVertices(float[] verts) {
		this.vertices = new float[verts.length];
		System.arraycopy(verts, 0, this.vertices, 0, verts.length);
		setVertexBuffer(this.vertices);
	}
	
	public float[] getVertices() {
		return this.vertices;
	}
	
	public void setNormalVertices(float[] nverts) {
		this.n_vertices = new float[nverts.length];
		System.arraycopy(nverts, 0, this.n_vertices, 0, nverts.length);
		setNormalBuffer(this.n_vertices);
	}
	
	public float[] getNormalVertices() {
		return this.n_vertices;
	}
	
	public void setFaces(short[] face) {
		this.indices = new short[face.length];
		System.arraycopy(face, 0, indices, 0, face.length);
		setFacesBuffer(this.indices);
	}
	
	public short[] getFaces() {
		return this.indices;
	}
	
	public void setTextures(int[] textures) {
		this.textures = new int[textures.length];
		System.arraycopy(textures, 0, this.textures, 0, textures.length);
	}
	
	public int[] getTextures() {
		return this.textures;
	}
	
	public String getImageSuffix() {
		return image_suffix;
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
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
		gl.glTexCoordPointer(2, GL10.GL_SHORT, 0, indexBuffer);
//		gl.glTranslatef(-6, -6, 1);
		gl.glDrawArrays(GL10.GL_POINTS, 0, vertices.length);
		
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}

	/**
	 * Loading texture
	 * 
	 * @param gl
	 *            the GL Context
	 */
	public void loadGLTexture(GL10 gl, Context context) {
		Bitmap texture = scaleTexture(image, 256);

		gl.glGenTextures(1, textures, 0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,GL10.GL_REPEAT);

		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, texture, 0);

		texture.recycle();
	}
	
	/**
	 * Scales image to valid texture
	 * 
	 * @param image
	 *            original image
	 * @param size
	 *            preferred width and height size
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		Bundle b = new Bundle();
		b.putFloatArray("vertices", vertices);
		b.putString("image_suffix", image_suffix);
		out.writeBundle(b);
	}
	
	public static final Parcelable.Creator<ObjectModel> CREATOR = new Creator<ObjectModel>() {
		
		@Override
		public ObjectModel[] newArray(int size) {
			return new ObjectModel[size];
		}
		
		@Override
		public ObjectModel createFromParcel(Parcel source) {
			return new ObjectModel(source);
		}
	};

}
