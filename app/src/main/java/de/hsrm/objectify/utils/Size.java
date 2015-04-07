package de.hsrm.objectify.utils;

public class Size {

    public int width;
    public int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }
}
