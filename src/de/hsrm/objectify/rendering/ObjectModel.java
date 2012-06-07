package de.hsrm.objectify.rendering;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

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
import de.hsrm.objectify.utils.Image;

/**
 * A representation of an actual object. Vertices, normals and texture can be
 * added after an instance of this class has been made. This class implements
 * {@link Parcelable} for sending between different Activities.
 * 
 * @author kwolf001
 * 
 */
public class ObjectModel implements Parcelable, Serializable {

	private static final long serialVersionUID = 1L;
	private static final String TAG = "ObjectModel";
	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer textureBuffer;
	private transient FloatBuffer normalBuffer;
	private transient ShortBuffer facesBuffer;

	private int[] textures = new int[1];

	private float[] texture;
	private float vertices[];
	private float normals[];
	private short faces[];
	private transient Image image;
	private float[] boundingbox;
	private byte[] bitmap_data;
	private int renderMode = GL10.GL_TRIANGLES;

	public ObjectModel(float[] vertices, float[] normals, short[] faces,
			Image image) {

		setVertices(vertices);
		setNormalVertices(normals);
		setFaces(faces);
		renderMode = GL10.GL_TRIANGLES;
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.rewind();

		byteBuf = ByteBuffer.allocateDirect(normals.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		normalBuffer = byteBuf.asFloatBuffer();
		normalBuffer.put(normals);
		normalBuffer.rewind();

		byteBuf = ByteBuffer.allocateDirect(faces.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		facesBuffer = byteBuf.asShortBuffer();
		facesBuffer.put(faces);
		facesBuffer.rewind();

		calcTextureCoords(image);
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.rewind();

		this.image = image.copy();
		// copying bitmap data to byte array for serialization purposes
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		image.compress(CompressFormat.PNG, 100, bos);
		byte[] bb = bos.toByteArray();
		bitmap_data = new byte[bb.length];
		System.arraycopy(bb, 0, bitmap_data, 0, bb.length);
	}

	private ObjectModel(Parcel source) {
		Bundle b = source.readBundle();
		renderMode = GL10.GL_TRIANGLES;
		textures = new int[1];
		setVertices(b.getFloatArray("vertices"));
		setNormalVertices(b.getFloatArray("normals"));
		setFaces(b.getShortArray("faces"));
		byte[] bb = b.getByteArray("image");
		bitmap_data = new byte[bb.length];
		System.arraycopy(bb, 0, bitmap_data, 0, bb.length);
		this.image = new Image(BitmapFactory.decodeByteArray(bb, 0, bb.length));

		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.rewind();

		byteBuf = ByteBuffer.allocateDirect(normals.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		normalBuffer = byteBuf.asFloatBuffer();
		normalBuffer.put(normals);
		normalBuffer.rewind();

		byteBuf = ByteBuffer.allocateDirect(faces.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		facesBuffer = byteBuf.asShortBuffer();
		facesBuffer.put(faces);
		facesBuffer.rewind();

		calcTextureCoords(this.image);
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.rewind();
	}

	private void calcTextureCoords(Image image) {
		int width = image.getWidth();
		int height = image.getHeight();
		ArrayList<Float> textcoords = new ArrayList<Float>();
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				float idx = (1.0f / (width - 1)) * y;
				float idy = (1.0f / (height - 1)) * x;
				textcoords.add(idx);
				textcoords.add(idy);
			}
		}
		this.texture = new float[textcoords.size()];
		for (int i = 0; i < textcoords.size(); i++) {
			texture[i] = textcoords.get(i);
		}
	}

	public void setVertices(float[] verts) {
		this.vertices = new float[verts.length];
		System.arraycopy(verts, 0, this.vertices, 0, verts.length);
		setVertexBuffer(this.vertices);
	}

	/**
	 * Needs to be called after object has been restored from hard disk.
	 */
	public void setup() {
		setVertices(vertices);
		setNormalVertices(normals);
		setFaces(faces);
		this.image = new Image(BitmapFactory.decodeByteArray(bitmap_data, 0,
				bitmap_data.length));
	}

	public int getTextureWidth() {
		return this.image.getWidth();
	}
	
	public int getTextureHeight() {
		return this.image.getHeight();
	}
	
	public Bitmap.Config getTextureBitmapConfig() {
		return this.image.getConfig();
	}
	public String getTextureBitmapSize() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.image.getWidth());
		sb.append("x");
		sb.append(this.image.getHeight());
		return sb.toString();
	}
	
	public byte[] getBitmapData() {
		return bitmap_data;
	}

	public float[] getVertices() {
		return this.vertices;
	}

	public void setNormalVertices(float[] nverts) {
		this.normals = new float[nverts.length];
		System.arraycopy(nverts, 0, this.normals, 0, nverts.length);
		setNormalBuffer(this.normals);
	}

	public float[] getNormalVertices() {
		return this.normals;
	}

	public void setFaces(short[] face) {
		this.faces = new short[face.length];
		System.arraycopy(face, 0, faces, 0, face.length);
		setFacesBuffer(this.faces);
	}

	public short[] getFaces() {
		return this.faces;
	}

	public void setTextures(int[] textures) {
		this.textures = new int[textures.length];
		System.arraycopy(textures, 0, this.textures, 0, textures.length);
	}

	public int[] getTextures() {
		return this.textures;
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

	private void setNormalBuffer(float[] normal) {
		ByteBuffer nbb = ByteBuffer.allocateDirect(normal.length * 4);
		nbb.order(ByteOrder.nativeOrder());
		this.normalBuffer = nbb.asFloatBuffer();
		for (float f : normals) {
			normalBuffer.put(f);
		}
		normalBuffer.rewind();
	}

	private void setFacesBuffer(short[] faces) {
		ByteBuffer fbb = ByteBuffer.allocateDirect(faces.length * 2);
		fbb.order(ByteOrder.nativeOrder());
		facesBuffer = fbb.asShortBuffer();
		for (short s : faces) {
			facesBuffer.put(s);
		}
		facesBuffer.rewind();
	}

	private float max(float[] values, int offset) {
		float maximum = values[offset];
		for (int i = offset; i < values.length; i += 3) {
			if (values[i] > maximum) {
				maximum = values[i];
			}
		}
		return maximum;
	}

	private float max(float[] values) {
		float maximum = values[0];
		for (int i = 1; i < values.length; i += 3) {
			if (values[i] > maximum) {
				maximum = values[i];
			}
		}
		return maximum;
	}

	private float min(float[] values, int offset) {
		float minimum = values[offset];
		for (int i = offset; i < values.length; i += 3) {
			if (values[i] < minimum) {
				minimum = values[i];
			}
		}
		return minimum;
	}

	/**
	 * Returns the bounding box for this object.
	 * 
	 * @return array with coordinates for lower left point and upper right point
	 *         in the order x1, x2, y1, y2, z1, z2.
	 */
	public float[] getBoundingBox() {
		if (boundingbox == null) {
			setupBoundingBox();
		}
		return boundingbox;
	}

	private void setupBoundingBox() {
		float x1 = 0, x2 = 0, y1 = 0, y2 = 0, z1 = 0, z2 = 0;
		x1 = min(vertices, 0);
		x2 = max(vertices, 0);
		y1 = min(vertices, 1);
		y2 = max(vertices, 1);
		z1 = min(vertices, 2);
		z2 = max(vertices, 2);
		boundingbox = new float[] { x1, x2, y1, y2, z1, z2 };
	}

	/**
	 * Returns the middle point of this objects' boundingbox
	 * 
	 * @return the middle point of this object.
	 */
	public float[] getMiddlePoint() {
		if (boundingbox == null) {
			setupBoundingBox();
		}
		float xmiddle = (boundingbox[0] + boundingbox[1]) / 2.0f;
		float ymiddle = (boundingbox[2] + boundingbox[3]) / 2.0f;
		float zmiddle = (boundingbox[4] + boundingbox[5]) / 2.0f;
		float[] middlepoint = new float[] { xmiddle, ymiddle, zmiddle };
		return middlepoint;
	}

	/**
	 * Returns the length of this object.
	 * 
	 * @return the length of this object.
	 */
	public float getLength() {
		if (boundingbox == null) {
			setupBoundingBox();
		}
		float[] tmp = new float[] { (boundingbox[1] - boundingbox[0]),
				(boundingbox[3] - boundingbox[2]),
				(boundingbox[5] - boundingbox[4]) };
		return 2.0f / max(tmp);
	}

	public void setRenderingMode(int renderMode) {
		this.renderMode = renderMode;
	}

	public void draw(GL10 gl) {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		gl.glDrawElements(renderMode, faces.length, GL10.GL_UNSIGNED_SHORT,
				facesBuffer);

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

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
				GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
				GL10.GL_LINEAR);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S,
				GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T,
				GL10.GL_REPEAT);

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
	private Bitmap scaleTexture(Image image, int size) {
		Bitmap bmp = Bitmap.createBitmap(image.getPixels(), image.getWidth(),
				image.getHeight(), image.getConfig());
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		float scaleWidth = ((float) size) / width;
		float scaleHeight = ((float) size) / height;
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		Bundle b = new Bundle();
		b.putFloatArray("vertices", vertices);
		b.putFloatArray("normals", normals);
		b.putShortArray("faces", faces);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		this.image.compress(Bitmap.CompressFormat.PNG, 100, baos);
		b.putByteArray("image", baos.toByteArray());
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
