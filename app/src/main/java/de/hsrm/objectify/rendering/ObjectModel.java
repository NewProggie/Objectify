package de.hsrm.objectify.rendering;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import de.hsrm.objectify.utils.ArrayUtils;

/**
 * A representation of a 3D model object. Vertices, mNormals and mTexture can be added after an
 * instance of this class is created. This class implements {@link Serializable} for
 * saving/loading an instance of it */
public class ObjectModel implements Serializable {

    private static final String TAG = "ObjectModel";
    private static final long serialVersionUID = 0L;
    private transient FloatBuffer mVertexBuffer;
    private transient FloatBuffer mTextureBuffer;
    private transient FloatBuffer normalsBuffer;
    private transient ShortBuffer mFacesBuffer;
    private transient Bitmap mTextureBitmap;
    private int[] mTextures = new int[1];
    private float[] mTexture;
    private float mVertices[];
    private float mNormals[];
    private short mFaces[];
    private float[] mBoundingBox;
    public byte[] mBitmapData;
    private final int mRenderMode = GL10.GL_TRIANGLES;

    public ObjectModel(float[] vertices, float[] normals, short[] faces) {
        onInitialize(vertices, normals, faces);
    }

    private void onInitialize(float[] vertices, float[] normals, short[] faces) {

        setVertices(vertices);
        setNormalVertices(normals);
        setFaces(faces);
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.rewind();

        byteBuf = ByteBuffer.allocateDirect(normals.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        normalsBuffer = byteBuf.asFloatBuffer();
        normalsBuffer.put(normals);
        normalsBuffer.rewind();

        byteBuf = ByteBuffer.allocateDirect(faces.length * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mFacesBuffer = byteBuf.asShortBuffer();
        mFacesBuffer.put(faces);
        mFacesBuffer.rewind();
    }

    public void setTextureBitmap(Bitmap texture) {
        mTextureBitmap = texture.copy(texture.getConfig(), true);
    }

    public int getVerticesSize() {
        return mVertices.length;
    }

    public int getFacesSize() {
        return mFaces.length;
    }

    public void setNormalVertices(float[] normals) {
        this.mNormals = new float[normals.length];
        System.arraycopy(normals, 0, this.mNormals, 0, normals.length);
        setNormalBuffer(this.mNormals);
    }

    public void setFaces(short[] face) {
        this.mFaces = new short[face.length];
        System.arraycopy(face, 0, mFaces, 0, face.length);
        setFacesBuffer(this.mFaces);
    }

    public short[] getFaces() {
        return mFaces;
    }

    private void setVertexBuffer(float[] vertices) {
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        this.mVertexBuffer = vbb.asFloatBuffer();
        for (float f : vertices) {
            mVertexBuffer.put(f);
        }
        mVertexBuffer.rewind();
    }

    private void setNormalBuffer(float[] normal) {
        ByteBuffer nbb = ByteBuffer.allocateDirect(normal.length * 4);
        nbb.order(ByteOrder.nativeOrder());
        this.normalsBuffer = nbb.asFloatBuffer();
        for (float f : mNormals) {
            normalsBuffer.put(f);
        }
        normalsBuffer.rewind();
    }

    private void setFacesBuffer(short[] faces) {
        ByteBuffer fbb = ByteBuffer.allocateDirect(faces.length * 2);
        fbb.order(ByteOrder.nativeOrder());
        mFacesBuffer = fbb.asShortBuffer();
        for (short s : faces) {
            mFacesBuffer.put(s);
        }
        mFacesBuffer.rewind();
    }

    public float getLength() {
        if (mBoundingBox == null) {
            setupBoundingBox();
        }
        float[] tmp = new float[] { (mBoundingBox[1] - mBoundingBox[0]),
                                    (mBoundingBox[3] - mBoundingBox[2]),
                                    (mBoundingBox[5] - mBoundingBox[4]) };
        return 2.0f / ArrayUtils.max(tmp);
    }

    private void setupBoundingBox() {
        float x1 = 0, x2 = 0, y1 = 0, y2 = 0, z1 = 0, z2 = 0;
        x1 = ArrayUtils.min(mVertices, 0);
        x2 = ArrayUtils.max(mVertices, 0);
        y1 = ArrayUtils.min(mVertices, 1);
        y2 = ArrayUtils.max(mVertices, 1);
        z1 = ArrayUtils.min(mVertices, 2);
        z2 = ArrayUtils.max(mVertices, 2);
        mBoundingBox = new float[] { x1, x2, y1, y2, z1, z2 };
    }

    /**
     * Returns the middle point of this objects' mBoundingBox
     *
     * @return the middle point of this object.
     */
    public float[] getMiddlePoint() {
        if (mBoundingBox == null) {
            setupBoundingBox();
        }
        float xmiddle = (mBoundingBox[0] + mBoundingBox[1]) / 2.0f;
        float ymiddle = (mBoundingBox[2] + mBoundingBox[3]) / 2.0f;
        float zmiddle = (mBoundingBox[4] + mBoundingBox[5]) / 2.0f;
        float[] middlepoint = new float[] { xmiddle, ymiddle, zmiddle };
        return middlepoint;
    }

    public void setVertices(float[] mVertices) {
        this.mVertices = new float[mVertices.length];
        System.arraycopy(mVertices, 0, this.mVertices, 0, mVertices.length);
        setVertexBuffer(this.mVertices);
    }

    public float[] getVertices() {
        return mVertices;
    }

    public void draw(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, normalsBuffer);

        gl.glDrawElements(mRenderMode, mFaces.length, GL10.GL_UNSIGNED_SHORT,
                mFacesBuffer);

        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {

        /* write vertices */
        out.writeInt(mVertices.length);
        for (int i = 0; i < mVertices.length; i++) {
            out.writeFloat(mVertices[i]);
        }

        /* write surface normals */
        out.writeInt(mNormals.length);
        for (int i = 0; i < mNormals.length; i++) {
            out.writeFloat(mNormals[i]);
        }

        /* write faces */
        out.writeInt(mFaces.length);
        for (int i = 0; i < mFaces.length; i++) {
            out.writeShort(mFaces[i]);
        }
        out.flush();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        /* read vertices */
        float[] vertices = new float[in.readInt()];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = in.readFloat();
        }

        /* read surface normals */
        float[] normals = new float[in.readInt()];
        for (int i = 0; i < normals.length; i++) {
            normals[i] = in.readFloat();
        }

        /* read faces */
        short[] faces = new short[in.readInt()];
        for (int i = 0; i < faces.length; i++) {
            faces[i] = in.readShort();
        }
        onInitialize(vertices, normals, faces);
    }

}
