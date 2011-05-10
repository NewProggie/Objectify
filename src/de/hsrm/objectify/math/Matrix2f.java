package de.hsrm.objectify.math;

public class Matrix2f {
	public float _11, _12;
	public float _21, _22;

	public float det() {
		return (this._11 * this._22) - (this._12 * this._21);
	}
	
}