package de.hsrm.objectify.rendering;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.interfaces.decomposition.SingularValueDecomposition;
import org.ejml.ops.CommonOps;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Calendar;

import de.hsrm.objectify.camera.Constants;
import de.hsrm.objectify.database.DatabaseAdapter;
import de.hsrm.objectify.database.DatabaseProvider;
import de.hsrm.objectify.rendering.compute_normals.ScriptC_compute_normals;
import de.hsrm.objectify.rendering.lh_integration.ScriptC_lh_integration;
import de.hsrm.objectify.utils.ArrayUtils;
import de.hsrm.objectify.utils.BitmapUtils;
import de.hsrm.objectify.utils.Storage;

public class ReconstructionService extends IntentService {

    public static final String DIRECTORY_NAME = "dir_name";
    public static final String NOTIFICATION = "de.hsrm.objectify.android.service.receiver";
    public static final String GALLERY_ID = "gallery_id";
    private static final int LH_ITERATIONS = 3000;
    private int mWidth;
    private int mHeight;

    public ReconstructionService() {
        super("ReconstructionService");
    }

    private ArrayList<Bitmap> readImages(String dirName) {
        ArrayList<Bitmap> images = new ArrayList<Bitmap>();
        /* i from 0 to number of images + ambient image */
        for (int i = 0; i <= Constants.NUM_IMAGES; i++) {
            Bitmap img = BitmapUtils.openBitmap(Storage.getExternalRootDirectory() +
                    "/" + dirName + "/" + Constants.IMAGE_NAME + i + "." + Constants.IMAGE_FORMAT);
            images.add(img);
        }

        return images;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        /* get images */
        String dirName = intent.getStringExtra(DIRECTORY_NAME);
        ArrayList<Bitmap> images = readImages(dirName);

        mWidth = images.get(0).getWidth();
        mHeight = images.get(0).getHeight();
        /* subtract first ambient image from the remaining images */
        Bitmap ambient = images.remove(0);
        for (int i = 0; i < images.size(); i++) {
            images.set(i, BitmapUtils.subtract(images.get(i), ambient));
        }

        /* compute normals */
        float[] normals = computeNormals(images,
                BitmapUtils.convertToGrayscale(BitmapUtils.binarize(images.get(2))));
        Bitmap Normals = BitmapUtils.convert(normals, mWidth, mHeight);
        BitmapUtils.saveBitmap(Normals, dirName, "normals.png");

        /* TODO: linear transformation depending on image size */
        float[] Z = localHeightfield(normals);
        Z = ArrayUtils.linearTransform(Z, 0.0f, 150.0f);
        int[] heightPixels = new int[mWidth*mHeight];
        int idx = 0;
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int z = (int) Z[i*mWidth+j];
                heightPixels[idx++] = Color.rgb(z, z, z);
            }
        }

        Bitmap Height = Bitmap.createBitmap(heightPixels, mWidth, mHeight, Bitmap.Config.ARGB_8888);
        BitmapUtils.saveBitmap(Height, dirName, "heights.png");

        Log.i("ReconstructionService", "createObjectModel");
        ObjectModel obj = createObjectModel(Z, normals, images.get(2));
        Log.i("ReconstructionService", "writeDatabaseEntry");
        String galleryId = writeDatabaseEntry(obj, images.get(3), dirName);

        /* clean up and publish results */
        Log.i("ReconstructionService", "publishResult");
        publishResult(galleryId);
    }

    private ObjectModel createObjectModel(float[] heights, float[] normals, Bitmap texture) {

        FloatBuffer vertBuf = FloatBuffer.allocate(mWidth * mHeight * 3);
        FloatBuffer normBuf = FloatBuffer.allocate(mWidth * mHeight * 3);
        ArrayList<Short> indexes = new ArrayList<Short>();
        vertBuf.rewind();
        normBuf.rewind();

        /* vertices and normals */
        int idx = 0;
        for (int y = 0; y < mHeight; y++) {
            for (int x = 0; x < mWidth; x++) {
                float[] imgPt = new float[] { Float.valueOf(x), Float.valueOf(y), heights[idx] };
                float[] nVec  = new float[] { normals[4*idx], normals[4*idx+1], normals[4*idx+2] };
                vertBuf.put(imgPt);
                normBuf.put(nVec);
                idx += 1;
            }
        }

        /* faces */
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                short index = (short) (j + (i * mWidth));
                indexes.add(index);
                indexes.add((short) (index + mWidth));
                indexes.add((short) (index + 1));

                indexes.add((short) (index + 1));
                indexes.add((short) (index + mWidth));
                indexes.add((short) (index + mWidth + 1));
            }
        }

        ShortBuffer indexBuf = ShortBuffer.allocate(indexes.size());
        indexBuf.rewind();
        for (int i = 0; i < indexes.size(); i++) {
            indexBuf.put(indexes.get(i));
        }

        Log.i("ReconstructionService", "vertices: " + vertBuf.array().length);
        Log.i("ReconstructionService", "normals: " + normBuf.array().length);
        Log.i("ReconstructionService", "faces: " + indexBuf.array().length);
        return new ObjectModel(vertBuf.array(), normBuf.array(), indexBuf.array(), texture);
    }

    private String writeDatabaseEntry(ObjectModel objectModel, Bitmap texture, String dirName) {

        /* initialize content resolver and database write */
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        Uri objUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("object").build();
        Uri galleryUri = DatabaseProvider.CONTENT_URI.buildUpon().appendPath("gallery").build();

        /* get timestamp for saving into database */
        Calendar cal = Calendar.getInstance();
        String date = String.valueOf(cal.getTimeInMillis());

        /* write 3d reconstruction to disk */
        String filePath = Storage.getExternalRootDirectory() + "/" + dirName + "/model.kaw";
        try {
            ObjectOutputStream objOutput = new ObjectOutputStream(new FileOutputStream(filePath));
            objOutput.writeObject(objectModel);
            objOutput.close();

            /* write object database entry */
            values.put(DatabaseAdapter.OBJECT_FILE_PATH_KEY, filePath);
            Uri objResultUri = cr.insert(objUri, values);
            String objectID = objResultUri.getLastPathSegment();
            values.clear();

            /* write texture to disk. TODO: maybe unnecessary, use existing one */
            String imgPath = Storage.getExternalRootDirectory() + "/" + dirName + "/texture.png";
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(imgPath));
            texture.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();

            /* write gallery database entry */
            values.put(DatabaseAdapter.GALLERY_IMAGE_PATH_KEY, dirName);
            values.put(DatabaseAdapter.GALLERY_DATE_KEY, date);
            values.put(DatabaseAdapter.GALLERY_DIMENSION_KEY, "640x480");
            values.put(DatabaseAdapter.GALLERY_FACES_KEY, "12345");
            values.put(DatabaseAdapter.GALLERY_VERTICES_KEY, "23456");
            values.put(DatabaseAdapter.GALLERY_OBJECT_ID_KEY, objectID);
            Uri galleryResultUri = cr.insert(galleryUri, values);
            return galleryResultUri.getLastPathSegment();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
        allInData.copyFromUnchecked(ArrayUtils.toFloatArray(EV.data));

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

    private void publishResult(String galleryId) {
        Log.i("ReconstructionService", "sendBroadcast");
        Intent publish = new Intent(NOTIFICATION);
        publish.putExtra(GALLERY_ID, galleryId);
        sendBroadcast(publish);
    }
}
