package de.hsrm.objectify.export;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.utils.Storage;

/**
 * Created by kai on 25.07.14.
 */
public class OBJExport {

    public static final String OBJ_NAME = "export.obj";

    public static boolean write(ObjectModel objectModel, Bitmap texture, String dirName) {

        String suffix = Storage.getExternalRootDirectory() + "/" + dirName + "/";
        String objPath = suffix + OBJ_NAME;
        String mtlPath = suffix + "objectify_model.mtl";
        String texPath = suffix + "objectify_model.jpg";

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(objPath));
            out.write("# Created by Objectify\n");
            out.write("mtllib objectify_model.mtl\n");
            float[] vertices = objectModel.getVertices();
            short[] faces = objectModel.getFaces();

            /* write vertices */
            for (int i = 0; i < vertices.length; i += 3) {
                out.write("v " + vertices[i] + " " + vertices[i+1] + " " + vertices[i+2] + "\n");
            }

            /* writing texture coords */
            int width = texture.getWidth();
            int height = texture.getHeight();
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
            byte[] bb = objectModel.mBitmapData;
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

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
