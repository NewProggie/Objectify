package de.hsrm.objectify.rendering;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.CommonOps;

import java.util.ArrayList;

import de.hsrm.objectify.camera.Constants;
import de.hsrm.objectify.rendering.compute_normals.ScriptC_compute_normals;
import de.hsrm.objectify.rendering.lh_integration.ScriptC_lh_integration;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.Storage;

public class ReconstructionService extends IntentService {

    public static final String IMAGE_PREFIX_NAME = "image_name";
    public static final String NOTIFICATION = "de.hsrm.objectify.android.service.receiver";
    public static final String NORMALMAP = "normalmap";
    private static final int LH_ITERATIONS = 3000;
    private int mWidth;
    private int mHeight;

    public ReconstructionService() {
        super("ReconstructionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String imagePrefix = intent.getStringExtra(IMAGE_PREFIX_NAME);

        /* read images */
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        /* i from 0 to Constants.NUM_IMAGES + ambient image */
//        for (int i = 0; i <= Constants.NUM_IMAGES; i++) {
//            Bitmap img = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
//                    "/" + imagePrefix + "_" + i + "." + Constants.IMAGE_FORMAT);
        for (int i = 0; i <= Constants.NUM_IMAGES; i++) {
            Bitmap img = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
                    "/kai_" + i + ".png");
            images.add(img);
        }

        mWidth = images.get(0).getWidth();
        mHeight = images.get(0).getHeight();
        /* subtract first ambient image from the remaining images */
//        Bitmap ambient = images.remove(0);
//        for (int i = 0; i < images.size(); i++) {
//            images.set(i, BitmapUtils.subtract(images.get(i), ambient));
//        }

        /* estimate threshold (otsu) */
        Bitmap Mask = BitmapUtils.convertToGrayscale(BitmapUtils.binarize(images.get(2)));

        long start = System.currentTimeMillis();
        float[] normals = computeNormals(images, Mask);
        Log.i("ReconstructionService", "computeNormals took: " + (System.currentTimeMillis() - start)/1000.0f + " sec.");
        Bitmap Normals = BitmapUtils.convert(normals, mWidth, mHeight);
        BitmapUtils.saveBitmap(Normals, "normals.png");

        start = System.currentTimeMillis();
        float[] Z = localHeightfield(normals);
        Log.i("ReconstructionService", "localHeightfield took: " + (System.currentTimeMillis() - start)/1000.0f + " sec.");

        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int row = 0; row < mHeight; row++) {
            for (int col = 0; col < mWidth; col++) {
                if (Z[row*mWidth+col] < min) min = Z[row*mWidth+col];
                if (Z[row*mWidth+col] > max) max = Z[row*mWidth+col];
            }
        }

        /* linear transformation of matrix valies from [min,max] -> [a,b] */
        float a = 0.0f, b = 200.0f;
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                Z[i*mWidth+j] = a + (b-a) * (Z[i*mWidth+j] - min) / (max - min);
            }
        }

        int[] heightPixels = new int[mWidth*mHeight];
        int idx = 0;
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int z = (int) Z[i*mWidth+j];
                heightPixels[idx++] = Color.rgb(z, z, z);
            }
        }

        BitmapUtils.saveBitmap(Bitmap.createBitmap(heightPixels, mWidth, mHeight, Bitmap.Config.ARGB_8888), "heights.png");

        /* clean up and publish results */
        publishResult(Storage.getExternalRootDirectory() + "/heights.png");
    }

    private float[] localHeightfield(float[] normals) {

        /* create RenderScript context used to communicate with RenderScript. Afterwards create the
         * actual script, which will do the real work */
        RenderScript rs = RenderScript.create(getApplicationContext());
        ScriptC_lh_integration lhIntegration = new ScriptC_lh_integration(rs);

        /* set params for the generator */
        lhIntegration.set_width(mWidth);
        lhIntegration.set_height(mHeight);

        /* create allocation input to RenderScript */
        Type normType = new Type.Builder(rs, Element.F32_4(rs)).setX(mWidth).setY(mHeight).create();
        Allocation allInNormals = Allocation.createTyped(rs, normType);
        allInNormals.copyFromUnchecked(normals);

        Type heightType = new Type.Builder(rs, Element.F32(rs)).setX(mWidth).setY(mHeight).create();
        Allocation allOutHeights = Allocation.createTyped(rs, heightType);

        /* bind normals and heights data to pNormals and pHeights pointer inside RenderScript */
        lhIntegration.bind_pNormals(allInNormals);
        lhIntegration.bind_pHeights(allOutHeights);

        /* pass the input to RenderScript */
        for (int i = 0; i < LH_ITERATIONS; i++) {
            lhIntegration.forEach_integrate(allInNormals, allOutHeights);
        }

        /* save output from RenderScript */
        float[] heights = new float[mWidth*mHeight];
        allOutHeights.copyTo(heights);

        return heights;
    }

    private float[] toFloatArray(double[] arr) {
        float[] floatArray = new float[arr.length];
        for (int i = 0 ; i < arr.length; i++) {
            floatArray[i] = (float) arr[i];
        }
        return floatArray;
    }

    private float[] computeNormals(ArrayList<Bitmap> images, Bitmap Mask) {

        /* populate A */
        double[][] a = new double[mWidth*mHeight][Constants.NUM_IMAGES];
        int[] imgData = new int[mWidth*mHeight];
        for (int k = 0; k < Constants.NUM_IMAGES; k++) {
            int idx = 0;
            images.get(k).getPixels(imgData, 0, mWidth, 0, 0, mWidth, mHeight);
            for (int i = 0; i < mHeight; i++) {
                for (int j = 0; j < mWidth; j++) {
                    int c = imgData[i*mWidth+j];
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

        /* create RenderScript context */
        RenderScript rs = RenderScript.create(getApplicationContext());
        ScriptC_compute_normals cmpNormals = new ScriptC_compute_normals(rs);

        /* set params for the generator */
        cmpNormals.set_width(mWidth);

        /* create allocation input to RenderScript */
        Type dataType = new Type.Builder(rs, Element.F32_4(rs)).setX(mWidth).setY(mHeight).create();
        Allocation allInData = Allocation.createTyped(rs, dataType);
        allInData.copyFromUnchecked(toFloatArray(EV.data));

        /* create allocation for masked image */
        Type maskType = new Type.Builder(rs, Element.I32(rs)).setX(mWidth).setY(mHeight).create();
        Allocation allMask = Allocation.createTyped(rs, maskType);
        int[] mask = new int[mWidth*mHeight];
        Mask.getPixels(mask, 0, mWidth, 0, 0, mWidth, mHeight);
        allMask.copyFrom(mask);

        /* bind pMask and pData pointer inside RenderScript */
        cmpNormals.bind_pMask(allMask);

        /* create allocation for output */
        Type normalsType = new Type.Builder(rs, Element.F32_4(rs)).setX(mWidth).setY(mHeight).create();
        Allocation allOutNormals = Allocation.createTyped(rs, normalsType);

        cmpNormals.forEach_compute_normals(allInData, allOutNormals);

        /* save output from RenderScript */
        float[] normals = new float[mWidth*mHeight*4];
        allOutNormals.copyTo(normals);

        return normals;
    }

    private void publishResult(String normalmap) {
        Intent publish = new Intent(NOTIFICATION);
        publish.putExtra(NORMALMAP, normalmap);
        sendBroadcast(publish);
    }
}
