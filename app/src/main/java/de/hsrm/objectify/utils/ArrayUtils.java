package de.hsrm.objectify.utils;

public class ArrayUtils {

    /**
     * Transform all array values from its current min, max range to a, b range
     * @param array array to transform
     * @param a new minimum value of array
     * @param b new maximum value of array
     * @return transformed array
     */
    public static float[] linearTransform(float[] array, float a, float b) {

        /* determine current min, max values */
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] < min) min = array[i];
            if (array[i] > max) max = array[i];
        }

        /* linear transformation of matrix values from [min,max] -> [a,b] */
        for (int i = 0; i < array.length; i++) {
            array[i] = a + (b - a) * (array[i] - min) / (max - min);
        }

        return array;
    }

}
