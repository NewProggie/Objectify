package de.hsrm.objectify.camera;

import android.graphics.Bitmap;

public interface Constants {

    public static final int IMAGE_RESOLUTION = 160*120;
    public static final int NUM_IMAGES = 4;
    public static final String IMAGE_NAME   = "image_";
    public static final String IMAGE_FORMAT = "png";
    public static final Bitmap.CompressFormat IMAGE_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;

    static class ReconstructionType {
        public static final int NORMALMAP       = 0;
        public static final int HEIGHTMAP       = 1;
        public static final int RECONSTRUCTION  = 2;
    }
}
