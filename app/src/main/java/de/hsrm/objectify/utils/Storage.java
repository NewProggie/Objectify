package de.hsrm.objectify.utils;

import android.os.Environment;

import java.io.File;
import java.util.Random;

public class Storage {

    private static final String DIRECTORY_NAME  = "/Android/data/de.hsrm.objectify";
    private static final String IMAGE_DIRECTORY = DIRECTORY_NAME + "/img";

    public static String getExternalRootDirectory() {
        return getDirectoryPath(DIRECTORY_NAME);
    }

    public static String getExternalImageDirectory() {
        return getDirectoryPath(IMAGE_DIRECTORY);
    }

    public boolean isMounted() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ? true : false;
    }

    public static String getRandomFileName(int size) {
        char[] chars = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ0123456789"
                .toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }

    private static String getDirectoryPath(String directory) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
            directory);
            if (dir.mkdirs() || dir.exists()) {
                return dir.getAbsolutePath();
            } else {
                throw new RuntimeException("Couldn't create external directory");
            }
        } else {
            throw new RuntimeException("External Storage is currently not available");
        }
    }
}
