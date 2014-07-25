package de.hsrm.objectify.export;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import de.hsrm.objectify.rendering.ObjectModel;
import de.hsrm.objectify.utils.Storage;

/**
 * Created by kai on 25.07.14.
 */
public class OBJExport {

    public static final String OBJ_NAME = "export.obj";

    public static boolean write(ObjectModel objectModel, String dirName) {

        String objPath = Storage.getExternalRootDirectory() + "/" + dirName + "/" + OBJ_NAME;

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(objPath));
            out.write("# Created by Objectify\n");
            float[] vertices = objectModel.getVertices();
            short[] faces = objectModel.getFaces();

            /* write vertices */
            for (int i = 0; i < vertices.length; i += 3) {
                out.write("v " + vertices[i] + " " + vertices[i+1] + " " + vertices[i+2] + "\n");
            }

            out.write("\n");
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }


//			/* writing texture coords */
//            int width = objectModel.getTextureWidth();
//            int height = objectModel.getTextureHeight();
//            for (int h = height-1; h >= 0; h--) {
//                for(int w = 0; w < width; w++) {
//                    out.write("vt " + (float) w/ (float) (width-1) + " " + (float) h/ (float) (height-1) + "\n");
//                }
//            }
//			/* writing faces */
//            out.write("usemtl picture\n");
//            for (int i = 0; i < faces.length; i += 3) {
//                int one = faces[i] + 1;
//                int two = faces[i + 1] + 1;
//                int three = faces[i + 2] + 1;
//                out.write("f " + one + "/" + one + " " + two + "/" + two + " " + three + "/" + three + "\n");
//            }
//            out.write("\n");
//            out.flush();
//            out.close();
//			/* Write texture image to jpg file */
//            byte[] bb = objectModel.getBitmapData();
//            Bitmap textureBitmap = BitmapFactory.decodeByteArray(bb, 0, bb.length);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            textureBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
//            FileOutputStream fout = new FileOutputStream(texPath);
//            fout.write(baos.toByteArray());
//			/* Create mtl for texture */
//            out = new BufferedWriter(new FileWriter(mtlPath));
//            out.write("newmtl picture\n");
//            out.write("map_Kd objectify_model.jpg\n");
//            out.flush();
//            out.close();
//			/* Creating zip file */
//            Compress pack = new Compress(new String[] { objPath, mtlPath, texPath }, zipPath);
//            pack.zip();
//            return zipPath;
//        } catch (IOException e) {
//            Log.e(TAG, e.getLocalizedMessage());
//        }
}
