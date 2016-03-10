/*
 * Objectify. Copyright (c) 2011-2016. Kai Wolf. All rights reserved.
 * Redistribution and use in source form with or without modification is not permitted.
 */

package de.hsrm.objectify.camera;

import android.graphics.Bitmap;

public interface Constants {
    int IMAGE_RESOLUTION = 120 * 160;
    int NUM_IMAGES = 4;
    String IMAGE_NAME = "image_";
    String IMAGE_FORMAT = "png";
    Bitmap.CompressFormat IMAGE_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;

    class ReconstructionType {
        public static final int NORMALMAP = 0;
        public static final int HEIGHTMAP = 1;
        public static final int RECONSTRUCTION = 2;
    }
}
