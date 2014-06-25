package de.hsrm.objectify.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

public class BitmapUtils {

    public static Bitmap convertToGrayscale(Bitmap src) {
        int width, height;
        height = src.getHeight();
        width = src.getWidth();
        Bitmap grayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(grayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(src, 0, 0, paint);

        return grayscale;
    }

    /**
     * Returns (binary) light pattern bitmap L_j {j = 1..N }
     * @param width width of display screen
     * @param height height of display screen
     * @param j current number of image
     * @param N total number of image
     * @return light patterned bitmap image
     */
    public static Bitmap generateLightPattern(int width, int height, int j, int N) {
        /* guaranteed to be initialized with zeros:
         * http://docs.oracle.com/javase/specs/jls/se7/html/jls-4.html#jls-4.12.5 */
        int[] pixels = new int[width*height];
        for (int y = -(height/2); y < height/2; y++) {
            for (int x = -(width/2); x < width/2; x++) {
                if (Math.signum(x*Math.cos(2*Math.PI*j/N) + y*Math.sin(2*Math.PI*j/N)) == 1.0) {
                    pixels[(y+height/2)*width+(x+width/2)] = Color.WHITE;
                }
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap generateLightPattern(Size screenSize, int j, int N) {
        return generateLightPattern(screenSize.width, screenSize.height, j, N);
    }

    public static void saveBitmap(Bitmap src, String filename) {
        String root = Environment.getExternalStorageDirectory().toString();
        File imageDir = new File(root + "/saved_images");
        imageDir.mkdirs();
        File file = new File(imageDir, filename);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            src.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
