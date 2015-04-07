package de.hsrm.objectify.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import java.util.List;

public class CameraUtils {

    public static Size determineTargetPictureSize(Camera.Parameters params, int desiredResolution) {

        List<Size> sizes = params.getSupportedPictureSizes();
        Size targetSize = sizes.get(0);
        int delta = Integer.MAX_VALUE;

        for (Size size : sizes) {
            int diff = Math.abs(desiredResolution - pixelCount(size));
            if (diff < delta) {
                targetSize = size;
                delta = diff;
            }
        }
        return targetSize;
    }

    public static Bitmap fixRotateMirrorImage(Bitmap src) {

        Matrix rotateRight = new Matrix();
        float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
        Matrix matrixMirrorY = new Matrix();
        matrixMirrorY.setValues(mirrorY);
        rotateRight.postConcat(matrixMirrorY);
        rotateRight.preRotate(270);
        final Bitmap rotMirrorImg = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(),
                rotateRight, true);
        src.recycle();

        return rotMirrorImg;
    }

    private static int pixelCount(Size size) {

        return size.width * size.height;
    }
}
