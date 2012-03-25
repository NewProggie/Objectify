package de.hsrm.objectify.math;

public class Matrix4f {
	// __ __
	// | |
	// | m11 m12 m13 m14 |
	// | m21 m22 m23 m24 |
	// | m31 m32 m33 m34 |
	// | m41 m42 m43 m44 |
	// |__ __|
	//
	public float _11, _12, _13, _14;
	public float _21, _22, _23, _24;
	public float _31, _32, _33, _34;
	public float _41, _42, _43, _44;

	/**
	 * Copies the values of a given Matrix4f.
	 * 
	 * @param pm
	 *            A matrix for copy.
	 */
	public void copy(Matrix4f pm) {
		_11 = pm._11;
		_12 = pm._12;
		_13 = pm._13;
		_14 = pm._14;
		_21 = pm._21;
		_22 = pm._22;
		_23 = pm._23;
		_24 = pm._24;
		_31 = pm._31;
		_32 = pm._32;
		_33 = pm._33;
		_34 = pm._34;
		_41 = pm._41;
		_42 = pm._42;
		_43 = pm._43;
		_44 = pm._44;
	}

	/**
	 * Constructs a 0 value matrix.
	 */
	public Matrix4f() {
		//
	}

	public void setIdentity() {
		_11 = _22 = _33 = _44 = 1.0f;
	}

	public void map(float[] pdata) {
		pdata[0] = _11;
		pdata[1] = _12;
		pdata[2] = _13;
		pdata[3] = _14;
		//
		pdata[4] = _21;
		pdata[5] = _22;
		pdata[6] = _23;
		pdata[7] = _24;
		//
		pdata[8] = _31;
		pdata[9] = _32;
		pdata[10] = _33;
		pdata[11] = _34;
		//
		pdata[12] = _41;
		pdata[13] = _42;
		pdata[14] = _43;
		pdata[15] = _44;
	}

	/**
	 * Constructs a matrix an another matrix.
	 */
	public Matrix4f(Matrix4f pmatrix) {
		this.copy(pmatrix);
	}

	/**
	 * Converts matrix to string.
	 */
	@Override
	public String toString() {
		String res = "";
		String tab = Character.toString((char) 9);

		res += this._11 + tab + this._12 + tab + this._13 + tab + this._14
				+ "\n";
		res += this._21 + tab + this._22 + tab + this._23 + tab + this._24
				+ "\n";
		res += this._31 + tab + this._32 + tab + this._33 + tab + this._34
				+ "\n";
		res += this._41 + tab + this._42 + tab + this._43 + tab + this._44
				+ "\n";

		return res;
	}

	// ----------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------

	/**
	 * Returns the determinant of a matrix build by the 4th row.
	 * 
	 * @param pm
	 *            The matrix.
	 */
	public static float determinant(Matrix4f pm) {

		float det;
		float det41, det42, det43, det44;
		float a41, a42, a43, a44;

		a41 = -pm._41;
		a42 = +pm._42;
		a43 = -pm._43;
		a44 = +pm._44;
		//
		// build the determinant of the adjunct matrices
		//
		det41 = ((pm._12 * pm._23 * pm._34) + (pm._13 * pm._24 * pm._32) + (pm._14
				* pm._22 * pm._33))
				- ((pm._14 * pm._23 * pm._32) + (pm._12 * pm._24 * pm._33) + (pm._13
						* pm._22 * pm._34));

		det42 = ((pm._11 * pm._23 * pm._34) + (pm._13 * pm._24 * pm._31) + (pm._14
				* pm._21 * pm._33))
				- ((pm._14 * pm._23 * pm._31) + (pm._11 * pm._24 * pm._33) + (pm._13
						* pm._21 * pm._34));

		det43 = ((pm._11 * pm._22 * pm._34) + (pm._12 * pm._24 * pm._31) + (pm._14
				* pm._21 * pm._32))
				- ((pm._14 * pm._22 * pm._31) + (pm._11 * pm._24 * pm._32) + (pm._12
						* pm._21 * pm._34));

		det44 = ((pm._11 * pm._22 * pm._33) + (pm._12 * pm._23 * pm._31) + (pm._13
				* pm._21 * pm._32))
				- ((pm._13 * pm._22 * pm._31) + (pm._11 * pm._23 * pm._32) + (pm._12
						* pm._21 * pm._33));
		//
		// build the determinant
		//
		det = (a41 * det41) + (a42 * det42) + (a43 * det43) + (a44 * det44);
		return det;
	}

	/**
	 * Create the inverse of a given matrix. Returns true if matrix is
	 * invertible false if matrix has determinant of 0. If false was returned
	 * this method has on effect on the destination matrix.
	 * 
	 * @param pm
	 *            The matrix which is to invert.
	 * @param pdest
	 *            The source matrix reference.
	 * @return The invertibility of the given matrix.
	 */
	public static boolean invert(Matrix4f pm, Matrix4f pdest) {

		float det;
		float idet;

		float a11, a12, a13, a14;
		float a21, a22, a23, a24;
		float a31, a32, a33, a34;
		float a41, a42, a43, a44;
		//
		// if determinant of matrix is 0, matrix is not regular
		//
		det = determinant(pm);
		if (det == 0)
			return false;
		//
		// build the reciprocal of the determinant
		//
		idet = 1.0f / det;
		//
		// build the adjuncts
		//
		a11 = ((pm._22 * pm._33 * pm._44) + (pm._23 * pm._34 * pm._42) + (pm._24
				* pm._32 * pm._43))
				- ((pm._24 * pm._33 * pm._42) + (pm._22 * pm._34 * pm._43) + (pm._23
						* pm._32 * pm._44));

		a21 = ((pm._21 * pm._33 * pm._44) + (pm._23 * pm._34 * pm._41) + (pm._24
				* pm._31 * pm._43))
				- ((pm._24 * pm._33 * pm._41) + (pm._21 * pm._34 * pm._43) + (pm._23
						* pm._31 * pm._44));

		a31 = ((pm._21 * pm._32 * pm._44) + (pm._22 * pm._34 * pm._41) + (pm._24
				* pm._31 * pm._42))
				- ((pm._24 * pm._32 * pm._41) + (pm._21 * pm._34 * pm._42) + (pm._22
						* pm._31 * pm._44));

		a41 = ((pm._21 * pm._32 * pm._43) + (pm._22 * pm._33 * pm._41) + (pm._23
				* pm._31 * pm._42))
				- ((pm._23 * pm._32 * pm._41) + (pm._21 * pm._33 * pm._42) + (pm._22
						* pm._31 * pm._43));
		//
		//
		a12 = ((pm._12 * pm._33 * pm._44) + (pm._13 * pm._34 * pm._42) + (pm._14
				* pm._32 * pm._43))
				- ((pm._14 * pm._33 * pm._42) + (pm._12 * pm._34 * pm._43) + (pm._13
						* pm._32 * pm._44));

		a22 = ((pm._11 * pm._33 * pm._44) + (pm._13 * pm._34 * pm._41) + (pm._14
				* pm._31 * pm._43))
				- ((pm._14 * pm._33 * pm._41) + (pm._11 * pm._34 * pm._43) + (pm._13
						* pm._31 * pm._44));

		a32 = ((pm._11 * pm._32 * pm._44) + (pm._12 * pm._34 * pm._41) + (pm._14
				* pm._31 * pm._42))
				- ((pm._14 * pm._32 * pm._41) + (pm._11 * pm._34 * pm._42) + (pm._12
						* pm._31 * pm._44));

		a42 = ((pm._11 * pm._32 * pm._43) + (pm._12 * pm._33 * pm._41) + (pm._13
				* pm._31 * pm._42))
				- ((pm._13 * pm._32 * pm._41) + (pm._11 * pm._33 * pm._42) + (pm._12
						* pm._31 * pm._43));
		//
		//
		a13 = ((pm._12 * pm._23 * pm._44) + (pm._13 * pm._24 * pm._42) + (pm._14
				* pm._22 * pm._43))
				- ((pm._14 * pm._23 * pm._42) + (pm._12 * pm._24 * pm._43) + (pm._13
						* pm._22 * pm._44));

		a23 = ((pm._11 * pm._23 * pm._44) + (pm._13 * pm._24 * pm._41) + (pm._14
				* pm._21 * pm._43))
				- ((pm._14 * pm._23 * pm._41) + (pm._11 * pm._24 * pm._43) + (pm._13
						* pm._21 * pm._44));

		a33 = ((pm._11 * pm._22 * pm._44) + (pm._12 * pm._24 * pm._41) + (pm._14
				* pm._21 * pm._42))
				- ((pm._14 * pm._22 * pm._41) + (pm._11 * pm._24 * pm._42) + (pm._12
						* pm._21 * pm._44));

		a43 = ((pm._11 * pm._22 * pm._43) + (pm._12 * pm._23 * pm._41) + (pm._13
				* pm._21 * pm._42))
				- ((pm._13 * pm._22 * pm._41) + (pm._11 * pm._23 * pm._42) + (pm._12
						* pm._21 * pm._43));
		//
		//
		a14 = ((pm._12 * pm._23 * pm._34) + (pm._13 * pm._24 * pm._32) + (pm._14
				* pm._22 * pm._33))
				- ((pm._14 * pm._23 * pm._32) + (pm._12 * pm._24 * pm._33) + (pm._13
						* pm._22 * pm._34));

		a24 = ((pm._11 * pm._23 * pm._34) + (pm._13 * pm._24 * pm._31) + (pm._14
				* pm._21 * pm._33))
				- ((pm._14 * pm._23 * pm._31) + (pm._11 * pm._24 * pm._33) + (pm._13
						* pm._21 * pm._34));

		a34 = ((pm._11 * pm._22 * pm._34) + (pm._12 * pm._24 * pm._31) + (pm._14
				* pm._21 * pm._32))
				- ((pm._14 * pm._22 * pm._31) + (pm._11 * pm._24 * pm._32) + (pm._12
						* pm._21 * pm._34));

		a44 = ((pm._11 * pm._22 * pm._33) + (pm._12 * pm._23 * pm._31) + (pm._13
				* pm._21 * pm._32))
				- ((pm._13 * pm._22 * pm._31) + (pm._11 * pm._23 * pm._32) + (pm._12
						* pm._21 * pm._33));
		//

		pdest._11 = (+a11) * idet;
		pdest._12 = (-a12) * idet;
		pdest._13 = (+a13) * idet;
		pdest._14 = (-a14) * idet;
		//
		pdest._21 = (-a21) * idet;
		pdest._22 = (+a22) * idet;
		pdest._23 = (-a23) * idet;
		pdest._24 = (+a24) * idet;
		//
		pdest._31 = (+a31) * idet;
		pdest._32 = (-a32) * idet;
		pdest._33 = (+a33) * idet;
		pdest._34 = (-a34) * idet;
		//
		pdest._41 = (-a41) * idet;
		pdest._42 = (+a42) * idet;
		pdest._43 = (-a43) * idet;
		pdest._44 = (+a44) * idet;

		return true;
	}

	/**
	 * Makes a given matrix to a identity matrix with a diagonal of 1.0f.
	 * 
	 * @param pm
	 *            The matrix to set.
	 */
	public static void identity(Matrix4f pm) {
		pm._11 = 1.0f;
		pm._12 = 0.0f;
		pm._13 = 0.0f;
		pm._14 = 0.0f;
		pm._21 = 0.0f;
		pm._22 = 1.0f;
		pm._23 = 0.0f;
		pm._24 = 0.0f;
		pm._31 = 0.0f;
		pm._32 = 0.0f;
		pm._33 = 1.0f;
		pm._34 = 0.0f;
		pm._41 = 0.0f;
		pm._42 = 0.0f;
		pm._43 = 0.0f;
		pm._44 = 1.0f;
	}

	/**
	 * Sets a matrix to a 0 matrix.
	 */
	public static void zero(Matrix4f pm) {
		pm._11 = 0.0f;
		pm._12 = 0.0f;
		pm._13 = 0.0f;
		pm._14 = 0.0f;
		pm._21 = 0.0f;
		pm._22 = 0.0f;
		pm._23 = 0.0f;
		pm._24 = 0.0f;
		pm._31 = 0.0f;
		pm._32 = 0.0f;
		pm._33 = 0.0f;
		pm._34 = 0.0f;
		pm._41 = 0.0f;
		pm._42 = 0.0f;
		pm._43 = 0.0f;
		pm._44 = 0.0f;
	}

	/**
	 * Returns a new a identity matrix with a diagonal of 1.0f.
	 */
	public static Matrix4f identity() {
		Matrix4f m = new Matrix4f();
		identity(m);
		return m;
	}

	/**
	 * Multiplies a matrix with another an stores the result in pmret.
	 * 
	 * @param pm1
	 *            The left operand matrix (A).
	 * @param pm2
	 *            The right operand matrix (B).
	 * @param pmret
	 *            The result matrix.
	 */
	public static void mul(Matrix4f pm1, Matrix4f pm2, Matrix4f pmret) {
		float c11, c12, c13, c14;
		float c21, c22, c23, c24;
		float c31, c32, c33, c34;
		float c41, c42, c43, c44;

		c11 = (pm1._11 * pm2._11) + (pm1._12 * pm2._21) + (pm1._13 * pm2._31)
				+ (pm1._14 * pm2._41);
		c12 = (pm1._11 * pm2._12) + (pm1._12 * pm2._22) + (pm1._13 * pm2._32)
				+ (pm1._14 * pm2._42);
		c13 = (pm1._11 * pm2._13) + (pm1._12 * pm2._23) + (pm1._13 * pm2._33)
				+ (pm1._14 * pm2._43);
		c14 = (pm1._11 * pm2._14) + (pm1._12 * pm2._24) + (pm1._13 * pm2._34)
				+ (pm1._14 * pm2._44);

		c21 = (pm1._21 * pm2._11) + (pm1._22 * pm2._21) + (pm1._23 * pm2._31)
				+ (pm1._24 * pm2._41);
		c22 = (pm1._21 * pm2._12) + (pm1._22 * pm2._22) + (pm1._23 * pm2._32)
				+ (pm1._24 * pm2._42);
		c23 = (pm1._21 * pm2._13) + (pm1._22 * pm2._23) + (pm1._23 * pm2._33)
				+ (pm1._24 * pm2._43);
		c24 = (pm1._21 * pm2._14) + (pm1._22 * pm2._24) + (pm1._23 * pm2._34)
				+ (pm1._24 * pm2._44);

		c31 = (pm1._31 * pm2._11) + (pm1._32 * pm2._21) + (pm1._33 * pm2._31)
				+ (pm1._34 * pm2._41);
		c32 = (pm1._31 * pm2._12) + (pm1._32 * pm2._22) + (pm1._33 * pm2._32)
				+ (pm1._34 * pm2._42);
		c33 = (pm1._31 * pm2._13) + (pm1._32 * pm2._23) + (pm1._33 * pm2._33)
				+ (pm1._34 * pm2._43);
		c34 = (pm1._31 * pm2._14) + (pm1._32 * pm2._24) + (pm1._33 * pm2._34)
				+ (pm1._34 * pm2._44);

		c41 = (pm1._41 * pm2._11) + (pm1._42 * pm2._21) + (pm1._43 * pm2._31)
				+ (pm1._44 * pm2._41);
		c42 = (pm1._41 * pm2._12) + (pm1._42 * pm2._22) + (pm1._43 * pm2._32)
				+ (pm1._44 * pm2._42);
		c43 = (pm1._41 * pm2._13) + (pm1._42 * pm2._23) + (pm1._43 * pm2._33)
				+ (pm1._44 * pm2._43);
		c44 = (pm1._41 * pm2._14) + (pm1._42 * pm2._24) + (pm1._43 * pm2._34)
				+ (pm1._44 * pm2._44);

		pmret._11 = c11;
		pmret._12 = c12;
		pmret._13 = c13;
		pmret._14 = c14;
		pmret._21 = c21;
		pmret._22 = c22;
		pmret._23 = c23;
		pmret._24 = c24;
		pmret._31 = c31;
		pmret._32 = c32;
		pmret._33 = c33;
		pmret._34 = c34;
		pmret._41 = c41;
		pmret._42 = c42;
		pmret._43 = c43;
		pmret._44 = c44;
	}

	/**
	 * Return a new matrix within the result of the first matrix multiplied with
	 * the second matrix.
	 * 
	 * @param pm1
	 *            The left operand matrix.
	 * @param pm2
	 *            The right operand matrix.
	 * @return A new matrix.
	 */
	public static Matrix4f mul(Matrix4f pm1, Matrix4f pm2) {
		Matrix4f m = new Matrix4f();
		mul(pm1, pm2, m);
		return m;
	}

	/**
	 * Multiplies a matrix with a vector and stores the result which also is a
	 * vector in pvret.
	 * 
	 * @param pm
	 *            The left operand matrix.
	 * @param pv
	 *            The right operand vector.
	 * @param pvret
	 *            The result vector.
	 */
	public static void mul(Matrix4f pm, Vector3f pv, Vector3f pvret) {
		float x;
		float y;
		float z;
		float w;
		float iw;

		x = (pm._11 * pv.x) + (pm._12 * pv.y) + (pm._13 * pv.z) + (pm._14);
		y = (pm._21 * pv.x) + (pm._22 * pv.y) + (pm._23 * pv.z) + (pm._24);
		z = (pm._31 * pv.x) + (pm._32 * pv.y) + (pm._33 * pv.z) + (pm._34);
		w = (pm._41 * pv.x) + (pm._42 * pv.y) + (pm._43 * pv.z) + (pm._44);

		iw = 1 / w;
		pvret.x = x * iw;
		pvret.y = y * iw;
		pvret.z = z * iw;
	}

	/**
	 * Multiplies a matrix with a vector and returns the result as new vector.
	 * 
	 * @param pm
	 *            The left operand matrix.
	 * @param pv
	 *            The right operand vector.
	 * @return A new vector.
	 */
	public static Vector3f mul(Matrix4f pm, Vector3f pv) {
		Vector3f v = new Vector3f();
		mul(pm, pv, v);
		return v;
	}

	/**
	 * Stores a matrix for rotation around the x-axis in pmret.
	 * 
	 * @param pmret
	 *            The matrix which is to fill.
	 * @param pangle
	 *            The angle of rotation.
	 */
	public static void rotateXMatrix(Matrix4f pmret, float pangle) {
		float cosa = (float) Math.cos(pangle);
		float sina = (float) Math.sin(pangle);

		identity(pmret);

		pmret._22 = cosa;
		pmret._23 = -sina;
		pmret._32 = sina;
		pmret._33 = cosa;
	}

	/**
	 * Returns a new matrix for rotation around the x-axis.
	 * 
	 * @param pangle
	 *            The angle of rotation.
	 */
	public static Matrix4f rotateXMatrix(float pangle) {
		Matrix4f m = new Matrix4f();
		rotateXMatrix(m, pangle);
		return m;
	}

	/**
	 * Stores a matrix for rotation around the y-axis in pmret.
	 * 
	 * @param pmret
	 *            The matrix which is to fill.
	 * @param pangle
	 *            The angle of rotation.
	 */
	public static void rotateYMatrix(Matrix4f pmret, float pangle) {
		float cosa = (float) Math.cos(pangle);
		float sina = (float) Math.sin(pangle);

		identity(pmret);

		pmret._11 = cosa;
		pmret._13 = sina;
		pmret._31 = -sina;
		pmret._33 = cosa;
	}

	/**
	 * Returns a new matrix for rotation around the y-axis.
	 * 
	 * @param pangle
	 *            The angle of rotation.
	 */
	public static Matrix4f rotateYMatrix(float pangle) {
		Matrix4f m = new Matrix4f();
		rotateYMatrix(m, pangle);
		return m;
	}

	/**
	 * Stores a matrix for rotation around the z-axis in pmret.
	 * 
	 * @param pmret
	 *            The matrix which is to fill.
	 * @param pangle
	 *            The angle of rotation.
	 */
	public static void rotateZMatrix(Matrix4f pmret, float pangle) {
		float cosa = (float) Math.cos(pangle);
		float sina = (float) Math.sin(pangle);

		identity(pmret);

		pmret._11 = cosa;
		pmret._12 = -sina;
		pmret._21 = sina;
		pmret._22 = cosa;
	}

	/**
	 * Returns a new matrix for rotation around the z-axis.
	 * 
	 * @param pangle
	 *            The angle of rotation.
	 */
	public static Matrix4f rotateZMatrix(float pangle) {
		Matrix4f m = new Matrix4f();
		rotateZMatrix(m, pangle);
		return m;
	}

	/**
	 * Stores a matrix for scale x, y, z in pmret.
	 * 
	 * @param pmret
	 *            The matrix which is to fill.
	 * @param px
	 *            The x scale factor.
	 * @param py
	 *            The y scale factor.
	 * @param pz
	 *            The z scale factor.
	 */
	public static void scaleMatrix(Matrix4f pmret, float px, float py, float pz) {
		identity(pmret);

		pmret._11 = px;
		pmret._22 = py;
		pmret._33 = pz;
	}

	/**
	 * Returns a new matrix for scale x, y, z
	 * 
	 * @param px
	 *            The x scale factor.
	 * @param py
	 *            The y scale factor.
	 * @param pz
	 *            The z scale factor.
	 */
	public static Matrix4f scaleMatrix(float px, float py, float pz) {
		Matrix4f m = new Matrix4f();
		scaleMatrix(m, px, py, pz);
		return m;
	}

	/**
	 * Stores a matrix for translate x, y, z in pmret.
	 * 
	 * @param pmret
	 *            The matrix which is to fill.
	 * @param px
	 *            The x translate offset.
	 * @param py
	 *            The y translate offset.
	 * @param pz
	 *            The z translate offset.
	 */
	public static void translateMatrix(Matrix4f pmret, float px, float py,
			float pz) {
		identity(pmret);

		pmret._14 = px;
		pmret._24 = py;
		pmret._34 = pz;
	}

	/**
	 * Returns a new matrix for translate x, y, z
	 * 
	 * @param px
	 *            The x translate offset.
	 * @param py
	 *            The y translate offset.
	 * @param pz
	 *            The z translate offset.
	 */
	public static Matrix4f translateMatrix(float px, float py, float pz) {
		Matrix4f m = new Matrix4f();
		translateMatrix(m, px, py, pz);
		return m;
	}

	public void setRotation(Quat4f q1) {
		float n, s;
		float xs, ys, zs;
		float wx, wy, wz;
		float xx, xy, xz;
		float yy, yz, zz;

		n = (q1.x * q1.x) + (q1.y * q1.y) + (q1.z * q1.z) + (q1.w * q1.w);
		s = (n > 0.0f) ? (2.0f / n) : 0.0f;

		xs = q1.x * s;
		ys = q1.y * s;
		zs = q1.z * s;
		wx = q1.w * xs;
		wy = q1.w * ys;
		wz = q1.w * zs;
		xx = q1.x * xs;
		xy = q1.x * ys;
		xz = q1.x * zs;
		yy = q1.y * ys;
		yz = q1.y * zs;
		zz = q1.z * zs;

		_11 = 1.0f - (yy + zz);
		_12 = xy - wz;
		_13 = xz + wy;
		_14 = 0f;
		_21 = xy + wz;
		_22 = 1.0f - (xx + zz);
		_23 = yz - wx;
		_24 = 0f;
		_31 = xz - wy;
		_32 = yz + wx;
		_33 = 1.0f - (xx + yy);
		_34 = 0f;
		_41 = 0f;
		_42 = 0f;
		_43 = 0f;
		_44 = 1f;

	}

}
