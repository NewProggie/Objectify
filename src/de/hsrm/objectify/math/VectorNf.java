package de.hsrm.objectify.math;

public class VectorNf {

	private float[] elements;

	public VectorNf(int units) {
		elements = new float[units];
	}

	public void set(int index, float element) {
		if (index >= elements.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		elements[index] = element;
	}

	public float get(int index) {
		if (index >= elements.length) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return elements[index];
	}

	public int getDimension() {
		return elements.length;
	}
}
