package de.hsrm.objectify.camera;

import java.util.Arrays;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class CompositePicture  {
	
	private static final String TAG = "CompositePicture";
	private byte[] right, up, left, down;
	
	public CompositePicture() {}
	
	public CompositePicture(CompositePicture other) {
		copy(other.right, this.right);
		copy(other.up, this.up);
		copy(other.left, this.left);
		copy(other.down, this.down);
	}
	
	public byte[] getRight() {
		return right;
	}
	public void setRight(byte[] right) {
		this.right = right;
	}
	public byte[] getUp() {
		return up;
	}
	public void setUp(byte[] up) {
		this.up = up;
	}
	public byte[] getLeft() {
		return left;
	}
	public void setLeft(byte[] left) {
		this.left = left;
	}
	public byte[] getDown() {
		return down;
	}
	public void setDown(byte[] down) {
		this.down = down;
	}
	
	private void copy(byte[] from, byte[] to) {
		for (int i=0;i<from.length;i++) {
			to[i] = from[i];
		}
	}

}
