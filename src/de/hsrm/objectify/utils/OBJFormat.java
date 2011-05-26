package de.hsrm.objectify.utils;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

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
	public static void writeFile(String path, ObjectModel objectModel) {
		try {
			FileWriter fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			float[] vertices = objectModel.getVertices();
			float[] n_vertices = objectModel.getNormalVertices();
			short[] faces = objectModel.getFaces();
			
			for (int i=0; i<vertices.length; i++) {
				if (i%3 == 0) 
					out.write("\nv " + String.valueOf(vertices[i]));
				else
					out.write(" " + String.valueOf(vertices[i]));
			}
			
			for (int i=0; i<n_vertices.length; i++) {
				if (i%3 == 0)
					out.write("\nvn " + String.valueOf(n_vertices[i]));
				else
					out.write(String.valueOf(n_vertices[i]));
			}
			out.flush();
			out.close();
			// TODO faces einlesen
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
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
