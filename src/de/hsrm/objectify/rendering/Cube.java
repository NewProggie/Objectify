package de.hsrm.objectify.rendering;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import de.hsrm.objectify.R;
import de.hsrm.objectify.utils.ExternalDirectory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.opengl.GLUtils;

/**
 * This class is an object representation of 
 * a Cube containing the vertex information,
 * texture coordinates, the vertex indices
 * and drawing functionality, which is called 
 * by the renderer.
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class Cube {

	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the texture coordinates */
	private FloatBuffer textureBuffer;
	/** The buffer holding the indices */
	private ByteBuffer indexBuffer;
		
	/** Our texture pointer */
	private int[] textures = new int[1];

	/** 
	 * The initial vertex definition
	 * 
	 * Note that each face is defined, even
	 * if indices are available, because
	 * of the texturing we want to achieve 
	 */	
    private float vertices[] = {
    					0.0f, 0.0f, 0.0f,
    					1.0f, 0.0f, 0.0f,
    					2.0f, 0.0f, 0.0f,
    					
    					0.0f, 1.0f, 0.0f,
    					1.0f, 1.0f, 0.0f,
    					2.0f, 1.0f, 0.0f,
    					
    					0.0f, 2.0f, 0.0f,
    					1.0f, 2.0f, 0.0f,
    					2.0f, 2.0f, 0.0f
    					//Vertices according to faces
//			    		-1.0f, -1.0f, 1.0f, //Vertex 0
//			    		1.0f, -1.0f, 1.0f,  //v1
//			    		-1.0f, 1.0f, 1.0f,  //v2
//			    		1.0f, 1.0f, 1.0f,   //v3
//			    		
//			    		1.0f, -1.0f, 1.0f,	//v4
//			    		1.0f, -1.0f, -1.0f, //v5	
//			    		1.0f, 1.0f, 1.0f, //v6
//			    		1.0f, 1.0f, -1.0f, //v7
//			    		
//			    		1.0f, -1.0f, -1.0f, //v8
//			    		-1.0f, -1.0f, -1.0f,//v9		
//			    		1.0f, 1.0f, -1.0f, //v10
//			    		-1.0f, 1.0f, -1.0f,//v11
//			    		
//			    		-1.0f, -1.0f, -1.0f,//v12
//			    		-1.0f, -1.0f, 1.0f, //v13	
//			    		-1.0f, 1.0f, -1.0f,//v14
//			    		-1.0f, 1.0f, 1.0f,//v15
//			    		
//			    		-1.0f, -1.0f, -1.0f,//v16
//			    		1.0f, -1.0f, -1.0f,//v17    		
//			    		-1.0f, -1.0f, 1.0f,//v18
//			    		1.0f, -1.0f, 1.0f,//v19
//			    		
//			    		-1.0f, 1.0f, 1.0f,//v20
//			    		1.0f, 1.0f, 1.0f,//v21	
//			    		-1.0f, 1.0f, -1.0f,//v22
//			    		1.0f, 1.0f, -1.0f,//v23
											};
    
    /** The initial texture coordinates (u, v) */	
    private float texture[] = {    		
			    		//Mapping coordinates for the vertices
			    		0.0f, 0.0f,
			    		0.5f, 0.0f,
			    		1.0f, 0.0f,
			    		
			    		0.0f, 0.5f,
			    		0.5f, 0.5f,
			    		1.0f, 0.5f,
			    		
			    		0.0f, 1.0f,
			    		0.5f, 1.0f,
			    		1.0f, 1.0f
			    		
//			    		0.0f, 0.0f,
//			    		0.0f, 1.0f,
//			    		1.0f, 0.0f,
//			    		1.0f, 1.0f,
//			    		
//			    		0.0f, 0.0f,
//			    		0.0f, 1.0f,
//			    		1.0f, 0.0f,
//			    		1.0f, 1.0f,
//			    		
//			    		0.0f, 0.0f,
//			    		0.0f, 1.0f,
//			    		1.0f, 0.0f,
//			    		1.0f, 1.0f,
//			    		
//			    		0.0f, 0.0f,
//			    		0.0f, 1.0f,
//			    		1.0f, 0.0f,
//			    		1.0f, 1.0f,
//			    		
//			    		0.0f, 0.0f,
//			    		0.0f, 1.0f,
//			    		1.0f, 0.0f,
//			    		1.0f, 1.0f,

			    							};
        
    /** The initial indices definition */	
    private byte indices[] = {
    					0,4,3, 0,1,4,
    					1,5,4, 1,2,5,
    					3,7,6, 3,4,7,
    					4,8,7, 4,5,8
    					
//    					//Faces definition
//			    		0,1,3, 0,3,2, 			//Face front
//			    		4,5,7, 4,7,6, 			//Face right
//			    		8,9,11, 8,11,10, 		//... 
//			    		12,13,15, 12,15,14, 	
//			    		16,17,19, 16,19,18, 	
//			    		20,21,23, 20,23,22, 	
    										};
    
    private String image_suffix;

	/**
	 * The Cube constructor.
	 * 
	 * Initiate the buffers.
	 */
	public Cube(String image_suffix) {
		
		this.image_suffix = image_suffix;
		// DEBUGGING START
		ArrayList<Float> verts = new ArrayList<Float>();
		for (int x=0;x<4;x++) {
			for (int y=0; y<4; y++) {
				verts.add(Float.valueOf(y));
				verts.add(Float.valueOf(x));
				verts.add(Float.valueOf(0));
			}
		}
		vertices = new float[verts.size()];
		for (int i=0; i< verts.size(); i++) {
			vertices[i] = verts.get(i);
		}
		
		ArrayList<Float> texts = new ArrayList<Float>();
		for (int x=0; x<4; x++) {
			for (int y=0; y<4; y++) {
				float idx = 1.0f/3*Float.valueOf(y);
				float idy = 1.0f/3*Float.valueOf(x);
				texts.add(idx);
				texts.add(idy);
			}
		}
		texture = new float[texts.size()];
		for (int i=0; i<texts.size(); i++) {
			float f = texts.get(i);
			texture[i] = f;
		}
		
		ArrayList<Byte> inds = new ArrayList<Byte>();
		for (int i=0; i<3; i++) {
			for (int j=0; j<3; j++) {
				short index = (byte) (j + (i*4));
				inds.add((byte) (index));
				inds.add((byte) (index+4));
				inds.add((byte) (index+1));
				
				inds.add((byte) (index+1));
				inds.add((byte) (index+4));
				inds.add((byte) (index+4+1));
			}
		}
		indices = new byte[inds.size()];
		for (int i=0; i<inds.size(); i++) {
			indices[i] = inds.get(i);
		}
		// DEBUGGING END
		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		//
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);

		//
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	/**
	 * The object own drawing function.
	 * Called from the renderer to redraw this instance
	 * with possible changes in values.
	 * 
	 * @param gl - The GL Context
	 */
	public void draw(GL10 gl) {
		//Bind our only previously generated texture in this case
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		
		//Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		//Set the face rotation
		gl.glFrontFace(GL10.GL_CCW);
		
		//Enable the vertex and texture state
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		
		//Draw the vertices as triangles, based on the Index Buffer information
		gl.glDrawElements(GL10.GL_TRIANGLES, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	
	/**
	 * Load the textures
	 * 
	 * @param gl - The GL Context
	 * @param context - The Activity context
	 */
	public void loadGLTexture(GL10 gl, Context context) {
		//Get the texture from the Android resource directory
		String path = ExternalDirectory.getExternalImageDirectory()+"/"+image_suffix+"_1.png";
		Bitmap image = BitmapFactory.decodeFile(path);
		Bitmap bitmap = scaleTexture(image, 256);

		//Generate one texture pointer...
		gl.glGenTextures(1, textures, 0);
		//...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		
		//Create Nearest Filtered Texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
		
		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		
		//Clean up
		bitmap.recycle();
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
}
