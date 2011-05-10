package de.hsrm.objectify.math;

public class Matrix3f {
	public float _11, _12, _13;
	public float _21, _22, _23;
	public float _31, _32, _33;
	
	public static Matrix3f identity(final Matrix3f pmatrix) {
		pmatrix._11 = 1.0f; pmatrix._12 = 0.0f; pmatrix._13 = 0.0f;
		pmatrix._21 = 0.0f; pmatrix._22 = 1.0f; pmatrix._23 = 0.0f;
		pmatrix._31 = 0.0f; pmatrix._32 = 0.0f; pmatrix._33 = 1.0f;
		return pmatrix;
	}
	
	public void map(final double[][] parray) {
		parray[0][0] = _11;
		parray[0][1] = _12;
		parray[0][2] = _13;
		parray[1][0] = _21;
		parray[1][1] = _22;
		parray[1][2] = _23;
		parray[2][0] = _31;
		parray[2][1] = _32;
		parray[2][2] = _33;
	}
	
	
	@Override
	public String toString() {
		return "" + _11 + "\t" + _12 + "\t" + _13 + "\n" +
		 		_21 + "\t" + _22 + "\t" + _23 + "\n" +
		 		_31 + "\t" + _32 + "\t" + _33;
	}
	
	public static Matrix3f identity() {
		return identity(new Matrix3f());
	}

	public float det() {
		return (_11*_22*_33) + (_12*_23*_31) + (_13*_21*_32) - (_11*_23*_32) - (_12*_21*_33) - (_13*_22*_31);
	}
	
	public static Matrix3f inverse(final Matrix3f psource) {
		return inverse(psource, new Matrix3f());
	}
	
	public static Matrix3f inverse(final Matrix3f psource, final Matrix3f pdest) {
	    //
		final float idet = 1.0f / psource.det();
		//
		pdest._11 = ((psource._22*psource._33) - (psource._23*psource._32))*idet;
		pdest._12 = ((psource._13*psource._32) - (psource._12*psource._33))*idet;
		pdest._13 = ((psource._12*psource._23) - (psource._13*psource._22))*idet;
		pdest._21 = ((psource._23*psource._31) - (psource._21*psource._33))*idet;
		pdest._22 = ((psource._11*psource._33) - (psource._13*psource._31))*idet;
		pdest._23 = ((psource._13*psource._21) - (psource._11*psource._23))*idet;
		pdest._31 = ((psource._21*psource._32) - (psource._22*psource._31))*idet;
		pdest._32 = ((psource._12*psource._31) - (psource._11*psource._32))*idet;
		pdest._33 = ((psource._11*psource._22) - (psource._12*psource._21))*idet;
		//
		return pdest;
	}
	
	public static Matrix3f scaleMatrix(final Matrix3f pmatrix, final float px, final float py) {
		identity(pmatrix);
		pmatrix._11 = px;
		pmatrix._22 = py;
		return pmatrix;
	}
	
	public static Matrix3f scaleMatrix(final float px, final float py) {
		return scaleMatrix(new Matrix3f(), px, py);
	}

	public static Matrix3f transpose(final Matrix3f psource, final Matrix3f pdest) {
		//
		float m11 = psource._11; 
		float m22 = psource._22; 
		float m33 = psource._33; 
		//
		float m12 = psource._21; 
		float m13 = psource._31; 
		//
		float m21 = psource._12; 
		float m23 = psource._32; 
		//
		float m31 = psource._13; 
		float m32 = psource._23; 
		//
		pdest._11 = m11;
		pdest._12 = m12;
		pdest._13 = m13;

		pdest._21 = m21;
		pdest._22 = m22;
		pdest._23 = m23;
		
		pdest._31 = m31;
		pdest._32 = m32;
		pdest._33 = m33;
		
		return pdest;
	}
	
	public static Matrix3f translateMatrix(final Matrix3f pmatrix, final float px, final float py) {
		identity(pmatrix);
		pmatrix._13 = px;
		pmatrix._23 = py;
		return pmatrix;
	}
	
	
	public static Matrix3f translateMatrix(final float px, final float py) {
		return translateMatrix(new Matrix3f(), px, py);
	}


	public static Matrix3f rotateMatrix(final Matrix3f pmatrix, final float pphi) {
		identity(pmatrix);
		pmatrix._11 = (float)Math.cos(pphi);
		pmatrix._12 = -(float)Math.sin(pphi);
		pmatrix._21 = (float)Math.sin(pphi);
		pmatrix._22 = (float)Math.cos(pphi);
		return pmatrix;
	}
	
	public static Matrix3f rotateMatrix(final float pphi) {
		return rotateMatrix(new Matrix3f(), pphi);
	}
	
	public static Matrix3f mul(final Matrix3f pm1, final Matrix3f pm2, final Matrix3f pmret) {
		//
		final float m11 = (pm1._11 * pm2._11) + (pm1._12 * pm2._21) + (pm1._13 * pm2._31);
		final float m12 = (pm1._11 * pm2._12) + (pm1._12 * pm2._22) + (pm1._13 * pm2._32);
		final float m13 = (pm1._11 * pm2._13) + (pm1._12 * pm2._23) + (pm1._13 * pm2._33);
		//
		final float m21 = (pm1._21 * pm2._11) + (pm1._22 * pm2._21) + (pm1._23 * pm2._31);
		final float m22 = (pm1._21 * pm2._12) + (pm1._22 * pm2._22) + (pm1._23 * pm2._32);
		final float m23 = (pm1._21 * pm2._13) + (pm1._22 * pm2._23) + (pm1._23 * pm2._33);
		//
		final float m31 = (pm1._31 * pm2._11) + (pm1._32 * pm2._21) + (pm1._33 * pm2._31);
		final float m32 = (pm1._31 * pm2._12) + (pm1._32 * pm2._22) + (pm1._33 * pm2._32);
		final float m33 = (pm1._31 * pm2._13) + (pm1._32 * pm2._23) + (pm1._33 * pm2._33);
		//
		pmret._11 = m11; pmret._12 = m12; pmret._13 = m13;
		pmret._21 = m21; pmret._22 = m22; pmret._23 = m23;
		pmret._31 = m31; pmret._32 = m32; pmret._33 = m33;
		//
		return pmret;
	}
	
	public static Matrix3f mul(final Matrix3f pm1, final Matrix3f pm2) {
		return mul(pm1, pm2, new Matrix3f());
	}
	
	public static Vector3f mul(final Matrix3f pm, Vector3f pvec, Vector3f pret) {
		//
		final float x = (pm._11 * pvec.x) + (pm._12 * pvec.y) + (pm._13 * pvec.z);
		final float y = (pm._21 * pvec.x) + (pm._22 * pvec.y) + (pm._23 * pvec.z);
		final float z = (pm._31 * pvec.x) + (pm._32 * pvec.y) + (pm._33 * pvec.z);
		//
		pret.x = x;
		pret.y = y;
		pret.z = z;
		//
		return pret;
	}
	
	public static Vector3f mul(final Matrix3f pm, Vector3f pvec) {
		return mul(pm, pvec, new Vector3f());
	}
	
	public static Vector2f mul(final Matrix3f pm, Vector2f pvec, Vector2f pret) {
		//
		final float x = (pm._11 * pvec.x) + (pm._12 * pvec.y) + (pm._13);
		final float y = (pm._21 * pvec.x) + (pm._22 * pvec.y) + (pm._23);
		final float iz = 1.0f / ((pm._31 * pvec.x) + (pm._32 * pvec.y) + (pm._33));
		//
		pret.x = x * iz;
		pret.y = y * iz;
		//
		return pret;
	}
	
	public static Vector2f mul(final Matrix3f pm, Vector2f pvec) {
		return mul(pm, pvec, new Vector2f());
	}
}
	