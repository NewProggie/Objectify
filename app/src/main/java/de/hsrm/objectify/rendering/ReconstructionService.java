package de.hsrm.objectify.rendering;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;

import java.util.ArrayList;

import de.hsrm.objectify.camera.Constants;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.Storage;

public class ReconstructionService extends IntentService {

    public static final String IMAGE_PREFIX_NAME = "image_name";
    public static final String NOTIFICATION = "de.hsrm.objectify.android.service.receiver";
    public static final String NORMALMAP = "normalmap";

    public ReconstructionService() {
        super("ReconstructionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String imagePrefix = intent.getStringExtra(IMAGE_PREFIX_NAME);


        /* read images */
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        for (int i = 0; i < Constants.NUM_IMAGES; i++) {
            Bitmap img = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
                    "/kai_" + i + ".png");
//            Bitmap img = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
//                    "/" + imagePrefix + "_" + i + "." + Constants.IMAGE_FORMAT);
            images.add(img);
        }

        Bitmap Mask = BitmapUtils.convertToGrayscale(BitmapUtils.binarize(images.get(0)));
        BitmapUtils.saveBitmap(Mask, "mask.png");
//        /* subtract first ambient image from the remaining images */
//        Bitmap ambient = images.remove(0);
//        for (int i = 0; i < images.size(); i++) {
//            images.set(i, BitmapUtils.subtract(images.get(i), ambient));
//        }

        int width = images.get(0).getWidth();
        int height = images.get(0).getHeight();
        Bitmap N = computeNormals(images, width, height);
        BitmapUtils.saveBitmap(N, "normals.png");
        Bitmap Z = localHeightfield(N, Mask);
        BitmapUtils.saveBitmap(Z, "heights.png");

        publishResult(Storage.getExternalRootDirectory()+"/normals.png");
    }

    private int index(int col, int row, int width) {
        return (row * width) + col;
    }

    private Bitmap localHeightfield(Bitmap Normals, Bitmap Mask) {
        int width = Normals.getWidth();
        int height = Normals.getHeight();
        float[][] Z = new float[width][height];

        int[] normals = new int[width*height];
        int[] mask = new int[width*height];
        Normals.getPixels(normals, 0, width, 0, 0, width, height);
        Mask.getPixels(mask, 0, width, 0, 0, width, height);

        for (int k = 0; k < 3000; k++) {
            Log.i("ReconstructionService", "k: " + k);
            for (int row = 1; row < height-1; row++) {
                for (int col = 1; col < width-1; col++) {
                    int up      = Color.red(mask[index(col,  row-1, width)]);
                    int down    = Color.red(mask[index(col,  row+1, width)]);
                    int left    = Color.red(mask[index(col-1,  row, width)]);
                    int right   = Color.red(mask[index(col+1,  row, width)]);
                    float zU = Z[col][row-1];
                    float zD = Z[col][row+1];
                    float zL = Z[col-1][row];
                    float zR = Z[col+1][row];
                    float nxC = Color.red(normals[index(col,   row, width)]);
                    float nyC = Color.green(normals[index(col, row, width)]);
                    float nxU = Color.red(normals[index(col, row-1, width)]);
                    float nyL = Color.green(normals[index(col-1, row, width)]);
                    if (up > 0 && down > 0 && left > 0 && right > 0)
                        Z[col][row] = (float) (1.0f/4.0f *
                                (zD + zU + zR + zL + nxU - nxC + nyL - nyC));
                }
            }
        }

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (Z[col][row] < min) min = Z[col][row];
                if (Z[col][row] > max) max = Z[col][row];
            }
        }
        Log.i("ReconstructionService", "min, max: " + min + ", " + max);

        /* linear transformation of matrix valies from [min,max] -> [a,b] */
        float a = 0.0f, b = 150.0f;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Z[j][i] = a + (b-a) * (Z[j][i] - min) / (max - min);
            }
        }

        int[] heightPixels = new int[width*height];
        int idx = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int z = (int) Z[j][i];
                heightPixels[idx++] = Color.rgb(z, z, z);
            }
        }

        return Bitmap.createBitmap(heightPixels, width, height, Bitmap.Config.ARGB_8888);
    }

    private Bitmap computeNormals(ArrayList<Bitmap> images, int width, int height) {

        double[][] a = new double[width*height][Constants.NUM_IMAGES];

        /* populate A */
        for (int k = 0; k < Constants.NUM_IMAGES; k++) {
            int idx = 0;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int c = images.get(k).getPixel(j, i);
                    a[idx++][k] = Color.red(c) + Color.green(c) + Color.blue(c);
                }
            }
        }

        DenseMatrix64F A = new DenseMatrix64F(a);
        SingularValueDecomposition<DenseMatrix64F> svd =
                DecompositionFactory.svd(A.numRows, A.numCols, true, true, true);

        /* TODO: catch java.lang.OutOfMemoryError */
        if (!svd.decompose(A)) {
            throw new RuntimeException("Decomposition failed");
        }

        DenseMatrix64F U = svd.getU(null, false);
        Bitmap S = android.graphics.Bitmap.createBitmap(
                width, height, android.graphics.Bitmap.Config.ARGB_8888);
        int idx = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double rSxyz = 1.0f / Math.sqrt(Math.pow(U.get(idx, 0), 2) +
                                      Math.pow(U.get(idx, 1), 2) +
                                      Math.pow(U.get(idx, 2), 2));
                /* U contanis eigenvectors of AAT, corresponding to z,x,y components of each
                 * pixels surface normal */
                int sz = (int) (128.0f + 127.0f * Math.signum(U.get(idx, 0)) *
                        Math.abs(U.get(idx, 0)) * rSxyz);
                int sx = (int) (128.0f + 127.0f * Math.signum(U.get(idx, 1)) *
                        Math.abs(U.get(idx, 1)) * rSxyz);
                int sy = (int) (128.0f + 127.0f * Math.signum(U.get(idx, 2)) *
                        Math.abs(U.get(idx, 2)) * rSxyz);
                S.setPixel(j, i, Color.rgb(sx, sy, sz));
                idx += 1;
               }
        }

        return S;
    }

    private void publishResult(String normalmap) {
        Intent publish = new Intent(NOTIFICATION);
        publish.putExtra(NORMALMAP, normalmap);
        sendBroadcast(publish);
    }
}
