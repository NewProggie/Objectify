package de.hsrm.objectify.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.File;
import java.io.FileOutputStream;

import de.hsrm.objectify.camera.Constants;

public class BitmapUtils {

    public static final double GS_RED     = 0.299;
    public static final double GS_GREEN   = 0.587;
    public static final double GS_BLUE    = 0.114;

    public static Bitmap convertToGrayscale(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width*height];

        src.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width*height; i++) {
            int p = pixels[i];
            int intensity = getIntensity(p);
            pixels[i] = Color.rgb(intensity, intensity, intensity);
        }

        return Bitmap.createBitmap(pixels, width, height, src.getConfig());
    }

    /** Return histogram of grayscaled image */
    public static int[] getHistogram(Bitmap bmp) {
        int[] histogram = new int[256];
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i <width*height; i++) {
            int c = Color.red(pixels[i]);
            histogram[c] += 1;
        }

        return histogram;
    }

    public static Bitmap binarize(Bitmap bmp) {
        int threshold = Math.round(getOtsuThreshold(bmp));
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width*height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width*height; i++) {
            int p = 0;
            if (Color.red(pixels[i]) > threshold) p = 255;
            pixels[i] = Color.rgb(p, p, p);
        }

        return Bitmap.createBitmap(pixels, width, height, bmp.getConfig());
    }

    public static Bitmap convert(float[] normals, int width, int height) {
        int[] pixels = new int[width*height];
        for (int i = 0; i < width*height; i++) {
            pixels[i] = Color.rgb((int) normals[4*i],
                                  (int) normals[4*i+1],
                                  (int) normals[4*i+2]);
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

    private static int getOtsuThreshold(Bitmap bmp) {
        int[] histogram = getHistogram(bmp);
        int pixelCount = bmp.getWidth()*bmp.getHeight();

        float sum = 0.0f;
        for (int i = 0; i < 256; i++)
            sum += i * histogram[i];

        float sumB = 0.0f;
        int wB = 0;
        int wF = 0;
        float varMax = 0.0f;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = pixelCount - wB;

            if (wF == 0) break;

            sumB += (float) (i * histogram[i]);
            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;
    }

    private static int getIntensity(int rgb) {
        return (int)(Color.red(rgb)   * BitmapUtils.GS_RED +
                     Color.green(rgb) * BitmapUtils.GS_GREEN +
                     Color.blue(rgb)  * BitmapUtils.GS_BLUE);
    }

    private static int clamp(int value) {
        if (value > 255) return 255;
        if (value < 0) return 0;
        return value;
    }

    public static Bitmap sum(Bitmap first, Bitmap second) {
        assert (first.getWidth()  == second.getWidth() &&
                first.getHeight() == second.getHeight());

        int width = first.getWidth();
        int height = first.getHeight();
        int[] firstPixels = new int[width*height];
        int[] secPixels = new int[width*height];
        first.getPixels(firstPixels, 0, width, 0, 0, width, height);
        second.getPixels(secPixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width*height; i++) {
            int srcPixel = getIntensity(firstPixels[i]);
            int otherPixel = getIntensity(secPixels[i]);
            int c = clamp(srcPixel + otherPixel);
            firstPixels[i] = Color.rgb(c, c, c);
        }

        return Bitmap.createBitmap(firstPixels, width, height, first.getConfig());
    }

    public static Bitmap subtract(Bitmap src, Bitmap ambient) {
        assert (src.getWidth()  == ambient.getWidth() &&
                src.getHeight() == ambient.getHeight());

        int width = src.getWidth();
        int height = src.getHeight();
        int[] srcPixels = new int[width*height];
        int[] ambPixels = new int[width*height];
        src.getPixels(srcPixels, 0, width, 0, 0, width, height);
        ambient.getPixels(ambPixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width*height; i++) {
            int srcPixel = getIntensity(srcPixels[i]);
            int otherPixel = getIntensity(ambPixels[i]);
            int c = clamp(srcPixel - otherPixel);
            srcPixels[i] = Color.rgb(c, c, c);
        }

        return Bitmap.createBitmap(srcPixels, width, height, src.getConfig());
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

    public static Bitmap generateBlackBitmap(Size screenSize) {
        /* guaranteed to be initialized with zeros. See generateLightPattern comment */
        int[] pixels = new int[screenSize.width*screenSize.height];
        return Bitmap.createBitmap(
                pixels, screenSize.width, screenSize.height, Bitmap.Config.ARGB_8888);
    }

    public static Bitmap openBitmap(String filepath) {
        return BitmapFactory.decodeFile(filepath);
    }

    public static void saveBitmap(Bitmap src, String dir, String filename) {
        File imageDirectory = new File(Storage.getExternalRootDirectory() + "/" + dir);
        imageDirectory.mkdirs();
        File file = new File(imageDirectory, filename);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            src.compress(Constants.IMAGE_COMPRESS_FORMAT, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
