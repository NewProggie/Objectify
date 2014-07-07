package de.hsrm.objectify.rendering;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import de.hsrm.objectify.utils.Size;

/**
 * A representation of a 3D model object. Vertices, normals and texture can be added after an
 * instance of this class is created. This class implements {@link android.os.Parcelable} for
 * sending an instance of it between different activities */
public class ObjectModel implements Parcelable, Serializable {

    private static final String TAG = "ObjectModel";
    private transient FloatBuffer vertexBuffer;
    private transient FloatBuffer textureBuffer;
    private transient FloatBuffer normalsBuffer;
    private transient ShortBuffer facesBuffer;
    private int[] textures = new int[1];
    private float[] texture;
    private float vertices[];
    private float normals[];
    private short faces[];
    private transient Bitmap bmp;
    private float[] boundingbox;
    private byte[] bitmapData;
    private final int renderMode = GL10.GL_TRIANGLES;

    public ObjectModel(float[] vertices, float[] normals, short[] faces, Bitmap bmp) {

        setVertices(vertices);
        setNormalVertices(normals);
        setFaces(faces);
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.rewind();

        byteBuf = ByteBuffer.allocateDirect(normals.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        normalsBuffer = byteBuf.asFloatBuffer();
        normalsBuffer.put(normals);
        normalsBuffer.rewind();

        byteBuf = ByteBuffer.allocateDirect(faces.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        facesBuffer = byteBuf.asShortBuffer();
        facesBuffer.put(faces);
        facesBuffer.rewind();

        computeTextureCoords(bmp);
        byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.rewind();

        this.bmp = bmp.copy(bmp.getConfig(), true);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bb = bos.toByteArray();
        bitmapData = new byte[bb.length];
        System.arraycopy(bb, 0, bitmapData, 0, bb.length);
    }

    private ObjectModel(Parcel source) {
        Bundle b = source.readBundle();
        textures = new int[1];
        /* TODO: Refactor into init() method */
        setVertices(b.getFloatArray("vertices"));
        setNormalVertices(b.getFloatArray("normals"));
        setFaces(b.getShortArray("faces"));
        byte[] bb = b.getByteArray("image");
        bitmapData = new byte[bb.length];
        System.arraycopy(bb, 0, bitmapData, 0, bb.length);
        this.bmp = bmp.copy(bmp.getConfig(), true);

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuf.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.rewind();

        byteBuf = ByteBuffer.allocateDirect(normals.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        normalsBuffer = byteBuf.asFloatBuffer();
        normalsBuffer.put(normals);
        normalsBuffer.rewind();

        byteBuf = ByteBuffer.allocateDirect(faces.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        facesBuffer = byteBuf.asShortBuffer();
        facesBuffer.put(faces);
        facesBuffer.rewind();

        computeTextureCoords(this.bmp);
        byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuf.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.rewind();
    }

    /**
     * Needs to be called right after object has been restored from hard disk
     */
    public void setup() {
        setVertices(vertices);
        setNormalVertices(normals);
        setFaces(faces);
        this.bmp = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
    }

    public void setNormalVertices(float[] normals) {
        this.normals = new float[normals.length];
        System.arraycopy(normals, 0, this.normals, 0, normals.length);
        setNormalBuffer(this.normals);
    }

    public void setFaces(short[] face) {
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

    private void setNormalBuffer(float[] normal) {
        ByteBuffer nbb = ByteBuffer.allocateDirect(normal.length * 4);
        nbb.order(ByteOrder.nativeOrder());
        this.normalsBuffer = nbb.asFloatBuffer();
        for (float f : normals) {
            normalsBuffer.put(f);
        }
        normalsBuffer.rewind();
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

    public Size getTextureSize() {
        return new Size(this.bmp.getWidth(), this.bmp.getHeight());
    }

    public void setVertices(float[] vertices) {
        this.vertices = new float[vertices.length];
        System.arraycopy(vertices, 0, this.vertices, 0, vertices.length);
        setVertexBuffer(this.vertices);
    }

    private void computeTextureCoords(Bitmap bmp) {
        /* TODO: Optimize me, no need for texCoords Array */
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        ArrayList<Float> texCoords = new ArrayList<Float>();
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                texCoords.add(1.0f / (width - 1.0f) * y);
                texCoords.add(1.0f / (height - 1.0f) * x);
            }
        }

        this.texture = new float[texCoords.size()];
        for (int i = 0; i <texCoords.size(); i++) {
            texture[i] = texCoords.get(i);
        }
    }

    public void draw(GL10 gl) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normalsBuffer);
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
        Bitmap texture = scaleTexture(this.bmp, 256);

        gl.glGenTextures(1, textures, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

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
    public void writeToParcel(Parcel parcel, int flags) {
        Bundle b = new Bundle();
        b.putFloatArray("vertices", vertices);
        b.putFloatArray("normals", normals);
        b.putShortArray("faces", faces);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        b.putByteArray("image", baos.toByteArray());
        parcel.writeBundle(b);
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
