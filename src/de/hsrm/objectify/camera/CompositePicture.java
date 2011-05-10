package de.hsrm.objectify.camera;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class CompositePicture  {
	
	private static final String TAG = "CompositePicture";
	private byte[] lo_pic, ro_pic, lu_pic, ru_pic;
	
	public byte[] getLo_pic() {
		return lo_pic;
	}
	public void setLo_pic(byte[] lo_pic) {
		this.lo_pic = lo_pic;
	}
	public byte[] getRo_pic() {
		return ro_pic;
	}
	public void setRo_pic(byte[] ro_pic) {
		this.ro_pic = ro_pic;
	}
	public byte[] getLu_pic() {
		return lu_pic;
	}
	public void setLu_pic(byte[] lu_pic) {
		this.lu_pic = lu_pic;
	}
	public byte[] getRu_pic() {
		return ru_pic;
	}
	public void setRu_pic(byte[] ru_pic) {
		this.ru_pic = ru_pic;
	}

}
