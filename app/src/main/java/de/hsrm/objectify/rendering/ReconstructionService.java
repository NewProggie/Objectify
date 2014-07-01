package de.hsrm.objectify.rendering;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.CommonOps;

import java.util.ArrayList;

import de.hsrm.objectify.camera.Constants;
import de.hsrm.objectify.rendering.lh_integration.ScriptC_lh_integration;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.Storage;

public class ReconstructionService extends IntentService {

    public static final String IMAGE_PREFIX_NAME = "image_name";
    public static final String NOTIFICATION = "de.hsrm.objectify.android.service.receiver";
    public static final String NORMALMAP = "normalmap";
    private static final int LH_ITERATIONS = 300;

    public ReconstructionService() {
        super("ReconstructionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String imagePrefix = intent.getStringExtra(IMAGE_PREFIX_NAME);

        /* read images */
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        /* i from 0 to Constants.NUM_IMAGES + ambient image */
        for (int i = 0; i <= Constants.NUM_IMAGES; i++) {
            Bitmap img = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
                    "/" + imagePrefix + "_" + i + "." + Constants.IMAGE_FORMAT);
            images.add(img);
        }

        int width = images.get(0).getWidth();
        int height = images.get(0).getHeight();
        /* subtract first ambient image from the remaining images */
        Bitmap ambient = images.remove(0);
        for (int i = 0; i < images.size(); i++) {
            images.set(i, BitmapUtils.subtract(images.get(i), ambient));
        }

        /* estimate threshold (otsu) */
        Bitmap Mask = BitmapUtils.convertToGrayscale(BitmapUtils.binarize(images.get(2)));

        Bitmap Normals = computeNormals(images, Mask, width, height);
        BitmapUtils.saveBitmap(Normals, "normals.png");

        Bitmap Heights = localHeightfield(Normals);
        BitmapUtils.saveBitmap(Heights, "heights.png");

        /* clean up and publish results */
        publishResult(Storage.getExternalRootDirectory() + "/normals.png");
    }

    private int index(int col, int row, int width) {
        return (row * width) + col;
    }

    private Bitmap localHeightfield(Bitmap Normals) {
        int width = Normals.getWidth();
        int height = Normals.getHeight();
        Bitmap Heights = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        /* create RenderScript context used to communicate with RenderScript. Afterwards create the
         * actual script, which will do the real work */
        RenderScript rs = RenderScript.create(getApplicationContext());
        ScriptC_lh_integration lhIntegration = new ScriptC_lh_integration(rs);

        /* set params for the generator */
        lhIntegration.set_width(width);
        lhIntegration.set_height(height);
        lhIntegration.set_iter(LH_ITERATIONS);

        /* create allocations (input/output) to RenderScript */
        Allocation allInNormals = Allocation.createFromBitmap(rs, Normals);
        Allocation allOutHeights = Allocation.createFromBitmap(rs, Heights);

        /* bind image data to pNormals and pHeights pointer inside the RenderScript */
        lhIntegration.bind_pNormals(allInNormals);
        lhIntegration.bind_pHeights(allOutHeights);

        /* pass the input to RenderScript */
        lhIntegration.forEach_integrate(allInNormals, allOutHeights);

        /* save output from RenderScript */
        allOutHeights.copyTo(Heights);

        return Heights;
    }

    private Bitmap computeNormals(ArrayList<Bitmap> images, Bitmap Mask, int width, int height) {

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
        CommonOps.transpose(A);
        SingularValueDecomposition<DenseMatrix64F> svd =
                DecompositionFactory.svd(A.numRows, A.numCols, false, true, true);

        /* TODO: catch java.lang.OutOfMemoryError */
        if (!svd.decompose(A)) {
            throw new RuntimeException("Decomposition failed");
        }

        /* speeding up computation, SVD from A^TA instead of AA^T */
        DenseMatrix64F EV = svd.getV(null, false);
        Bitmap S = android.graphics.Bitmap.createBitmap(
                width, height, android.graphics.Bitmap.Config.ARGB_8888);
        int[] mask = new int[width*height];
        Mask.getPixels(mask, 0, width, 0, 0, width, height);
        int idx = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double rSxyz = 1.0f / Math.sqrt(Math.pow(EV.get(idx, 0), 2) +
                                                Math.pow(EV.get(idx, 1), 2) +
                                                Math.pow(EV.get(idx, 2), 2));
                /* EV contains the eigenvectors of A^TA, which are as well the z,x,y components of
                 * the surface normals for each pixel */
                int sz = (int) (128.0f + 127.0f * Math.signum(EV.get(idx, 0)) *
                        Math.abs(EV.get(idx, 0)) * rSxyz);
                int sx = (int) (128.0f + 127.0f * Math.signum(EV.get(idx, 1)) *
                        Math.abs(EV.get(idx, 1)) * rSxyz);
                int sy = (int) (128.0f + 127.0f * Math.signum(EV.get(idx, 2)) *
                        Math.abs(EV.get(idx, 2)) * rSxyz);
                /* FIXME: optimize, dont use setPixel */
                if (mask[index(j, i, width)] == Color.WHITE)
                    S.setPixel(j, i, Color.rgb(sx, sy, sz));
                else {
                    S.setPixel(j, i, Color.rgb(0, 0, 255));
                }
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
