package de.hsrm.objectify.math;

public class Vector2f {
	public float x = 0.0f;
	public float y = 0.0f;
	
	public Vector2f() {
		//
	}
	
    /**
     * Normalizes a vector and stores the result that is a vector
     * in the same direction with length of 1 in pvret.
     * @param pv Source vector.
     * @param pvret Normalized vector.
     */
    public static void normalize(Vector2f pv, Vector2f pvret) {
        float l = 1.0f / pv.length();
        pvret.x = pv.x * l;
        pvret.y = pv.y * l;
    }
    /**
     * Normalizes a vector and returns the result that is a vector
     * in the same direction with length of 1 as a new vector.
     * @param pv The source vector.
     * @return A new normalized vector.
     */
    public static Vector2f normalize(Vector2f pv) {
        float l = 1.0f / pv.length();
        return new Vector2f(pv.x * l, pv.y * l);
    }

	public Vector2f(float px, float py) {
		this.x = px;
		this.y = py;
	}
	
	public Vector2f(Vector2f pv) {
		this.x = pv.x;
		this.y = pv.y;
	}
	
	
	public void copy(Vector2f pv) {
		this.x = pv.x;
		this.y = pv.y;
	}
	
	public Vector2f copy() {
		return new Vector2f(this.x, this.y);
	}
	
	public void copy(float[] pd) {
		this.x = pd[0];
		this.y = pd[1];
	}
	
	public void copy(float[] pd, int poffset) {
		this.x = pd[poffset];
		this.y = pd[poffset + 1];
	}
	@Override
	public String toString() {
		return "Vector2f(" + this.x + "," + this.y + ")";
	}
	
	public float length() {
		return (float)Math.sqrt((this.x * this.x) + (this.y * this.y));
	}

	public float length2() {
		return ((this.x * this.x) + (this.y * this.y));
	}
	
    public static void add(Vector2f pv1, Vector2f pv2, Vector2f pvret) {
        pvret.x = pv1.x + pv2.x;
        pvret.y = pv1.y + pv2.y;
    }

    public static Vector2f add(Vector2f pv1, Vector2f pv2) {
        return new Vector2f(pv1.x + pv2.x, pv1.y + pv2.y);
    }
    
    public static void sub(Vector2f pv1, Vector2f pv2, Vector2f pvret) {
        pvret.x = pv1.x - pv2.x;
        pvret.y = pv1.y - pv2.y;
    }

    public static Vector2f sub(Vector2f pv1, Vector2f pv2) {
        return new Vector2f(pv1.x - pv2.x, pv1.y - pv2.y);
    }

	public static void mul(Vector2f pv, float pscalar, Vector2f presult) {
		presult.x = pv.x * pscalar;
		presult.y = pv.y * pscalar;
	}
    /**
     * Returns the scalar product of two vectors.
     * @param pv1 Left operand vector.
     * @param pv2 Right operand vector.
     * @return The scalar product.
     */
    public static float scalar(Vector2f pv1, Vector2f pv2) {
        return (pv1.x * pv2.x) + (pv1.y * pv2.y);
    }

    /**
     * Returns the angle (phi) between two vectors.
     * @param pv1 The first vector.
     * @param pv2 The second vector.
     * @return The angle.
     */
    public static float phi(Vector2f pv1, Vector2f pv2) {
        return (float)Math.acos(scalar(pv1, pv2) / 
                                (pv1.length() * pv2.length()));
    }
	
	public static Vector2f mul(Vector2f pv, float pscalar) {
		return new Vector2f(pv.x * pscalar, pv.y * pscalar);
	}
    
			
}