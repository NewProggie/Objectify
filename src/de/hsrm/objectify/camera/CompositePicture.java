package de.hsrm.objectify.camera;

/**
 * This class holds all four pictures taken from camera. The data here is used
 * for calculating an object out of the 2d data.
 * 
 * @author kwolf001
 * 
 */
public class CompositePicture {

	private static final String TAG = "CompositePicture";
	private byte[] picture1, picture2, picture3, picture4;

	public CompositePicture() {}

	public byte[] getPicture1() {
		return picture1;
	}

	public void setPicture1(byte[] picture1) {
		this.picture1 = picture1;
	}

	public byte[] getPicture2() {
		return picture2;
	}

	public void setPicture2(byte[] picture2) {
		this.picture2 = picture2;
	}

	public byte[] getPicture3() {
		return picture3;
	}

	public void setPicture3(byte[] picture3) {
		this.picture3 = picture3;
	}

	public byte[] getPicture4() {
		return picture4;
	}

	public void setPicture4(byte[] picture4) {
		this.picture4 = picture4;
	} 
	
	
}
