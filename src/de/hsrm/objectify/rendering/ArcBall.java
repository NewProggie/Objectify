package de.hsrm.objectify.rendering;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;
import de.hsrm.objectify.math.Quat4f;
import de.hsrm.objectify.math.Vector3f;

/**
 * This class implements an arcball rotation for the objectviewer
 */
public class ArcBall {
	private static final float Epsilon = 1.0e-5f;

	Vector3f StVec;
	Vector3f EnVec;
	float adjustWidth;
	float adjustHeight;

	/**
	 * creates new arcball
	 * 
	 * @param width
	 *            arcball width
	 * @param height
	 *            arcball height
	 */
	public ArcBall(float width, float height) {
		StVec = new Vector3f();
		EnVec = new Vector3f();
		setBounds(width, height);
	}

	/**
	 * maps finger touch onto arcballs sphere
	 * 
	 * @param point
	 *            finger touch
	 * @param vector
	 *            vector from objects middlepoint to fingertouch
	 */
	public void mapToSphere(Point point, Vector3f vector) {
		PointF tempPoint = new PointF(point.x, point.y);

		tempPoint.x = (tempPoint.x * this.adjustWidth) - 1.0f;
		tempPoint.y = 1.0f - (tempPoint.y * this.adjustHeight);

		float length = (tempPoint.x * tempPoint.x) + (tempPoint.y * tempPoint.y);

		if (length > 1.0f) {
			float norm = (float) (1.0 / Math.sqrt(length));
			vector.x = tempPoint.x * norm;
			vector.y = tempPoint.y * norm;
			vector.z = 0.0f;
		} else {
			vector.x = tempPoint.x;
			vector.y = tempPoint.y;
			vector.z = (float) Math.sqrt(1.0f - length);
		}

	}

	/**
	 * sets new bounds for arcball
	 * @param width new width
	 * @param height new height
	 */
	public void setBounds(float width, float height) {
		assert ((width > 1.0f) && (height > 1.0f));

		adjustWidth = 1.0f / ((width - 1.0f) * 0.5f);
		adjustHeight = 1.0f / ((height - 1.0f) * 0.5f);
	}

	public void click(Point NewPt) {
		mapToSphere(NewPt, this.StVec);

	}

	/**
	 * finger dragging while rotating with arcball
	 * @param NewPt new point
	 * @param NewRot new rotation
	 */
	public void drag(Point NewPt, Quat4f NewRot) {

		this.mapToSphere(NewPt, EnVec);

		if (NewRot != null) {
			Vector3f Perp = new Vector3f();

			Vector3f.cross(StVec, EnVec, Perp);

			if (Perp.length() > Epsilon) {
				NewRot.x = Perp.x;
				NewRot.y = Perp.y;
				NewRot.z = Perp.z;
				NewRot.w = Vector3f.dot(StVec, EnVec);
			} else {
				NewRot.x = NewRot.y = NewRot.z = NewRot.w = 0.0f;
			}
		}
	}
}
