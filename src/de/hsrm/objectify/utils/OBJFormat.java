package de.hsrm.objectify.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import de.hsrm.objectify.rendering.ObjectModel;

/**
 * This class takes care of creating and reading <a
 * href="http://www.martinreddy.net/gfx/3d/OBJ.spec">obj files</a> and saving it
 * respectively loading it from external storage.
 * 
 * @author kwolf001
 * 
 */
public class OBJFormat {

	private final static String TAG = "OBJFormat";

	/**
	 * Creates a new obj file and stores it onto the given path
	 * 
	 * @param path
	 *            path to external storage
	 * @param objectModel
	 *            object which will be written to the obj file
	 */
	public static String writeFile(ObjectModel objectModel) {
		try {
			// writing obj for export
			String suffix = ExternalDirectory.getExternalRootDirectory();
			String objPath = suffix + "/objectify_model.obj";
			String mtlPath = suffix + "/objectify_model.mtl";
			String texPath = suffix + "/objectify_model.jpg";
			String zipPath = suffix + "/objectify_model.zip";
			
			BufferedWriter out = new BufferedWriter(new FileWriter(objPath));
			out.write("# Created by Objectify\n");
			out.write("mtllib objectify_model.mtl\n");
			float[] vertices = objectModel.getVertices();
			short[] faces = objectModel.getFaces();
			/* writing vertices */
			for (int i = 0; i < vertices.length; i += 3) {
				out.write("v " + vertices[i] + " " + vertices[i + 1] + " "
						+ vertices[i + 2] + "\n");
			}
			/* writing texture coords */
			int width = objectModel.getTextureWidth();
			int height = objectModel.getTextureHeight();
			for (int h = height-1; h >= 0; h--) {
				for(int w = 0; w < width; w++) {
					out.write("vt " + (float) w/ (float) (width-1) + " " + (float) h/ (float) (height-1) + "\n");
				}
			}
			/* writing faces */
			out.write("usemtl picture\n");
			for (int i = 0; i < faces.length; i += 3) {
				int one = faces[i] + 1;
				int two = faces[i + 1] + 1;
				int three = faces[i + 2] + 1;
				out.write("f " + one + "/" + one + " " + two + "/" + two + " " + three + "/" + three + "\n");
			}
			out.write("\n");
			out.flush();
			out.close();
			/* Write texture image to jpg file */
			byte[] bb = objectModel.getBitmapData();
			Bitmap textureBitmap = BitmapFactory.decodeByteArray(bb, 0, bb.length);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			textureBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
			FileOutputStream fout = new FileOutputStream(texPath);
			fout.write(baos.toByteArray());
			/* Create mtl for texture */
			out = new BufferedWriter(new FileWriter(mtlPath));
			out.write("newmtl picture\n");
			out.write("map_Kd objectify_model.jpg\n");
			out.flush();
			out.close();
			/* Creating zip file */
			Compress pack = new Compress(new String[] { objPath, mtlPath, texPath }, zipPath);
			pack.zip();
			return zipPath;
		} catch (IOException e) {
			Log.e(TAG, e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * Returns a new created {@link ObjectModel} from the given path to an obj
	 * file. Can return null in case the file is corrupt.
	 * 
	 * @param path
	 *            path to an obj file on the external storage
	 * @return a new ObjectModel or null if fill is corrupt
	 */
	public static ObjectModel readFile(String path) {
		try {
			FileInputStream fin = new FileInputStream(path);

		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
			return null;
		}
		return null;
	}

}
