package de.hsrm.objectify.rendering;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

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
        for (int i = 0; i <= Constants.NUM_IMAGES; i++) {
            Bitmap img = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
                    "/" + imagePrefix + "_" + i + "." + Constants.IMAGE_FORMAT);
            images.add(img);
        }

        int width = images.get(0).getWidth();
        int height = images.get(0).getHeight();

        /* subtract first ambient image */
        Bitmap ambient = images.remove(0);


        Bitmap N = computeNormals(images, width, height);
        BitmapUtils.saveBitmap(N, "normals.png");

        publishResult(Storage.getExternalRootDirectory()+"/normals.png");
    }

    private Bitmap computeNormals(ArrayList<Bitmap> images, int width, int height) {
        final double GS_RED     = 0.299;
        final double GS_GREEN   = 0.587;
        final double GS_BLUE    = 0.114;
        double[][] a = new double[width*height][Constants.NUM_IMAGES];

        /* populate A */
        for (int k = 0; k < Constants.NUM_IMAGES; k++) {
            int idx = 0;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    /* retrieve pixel intensity */
                    int c = images.get(k).getPixel(j, i);
                    a[idx++][k] = (Color.red(c) * GS_RED +
                                   Color.green(c) * GS_GREEN +
                                   Color.blue(c) * GS_BLUE);
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
