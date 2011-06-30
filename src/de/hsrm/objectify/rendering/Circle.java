package de.hsrm.objectify.rendering;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * A vertex based Circle
 * 
 * @author kwolf001
 * 
 */
public class Circle {

	private FloatBuffer vertexBuffer;

	/**
	 * Creates a new circle from given radius
	 * 
	 * @param radius
	 *            radius of circle in centimeter (not implemented yet)
	 */
	public Circle(float radius) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(720 * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		for (int i = 0; i < 720; i += 2) {
			Double x = Math.cos(Math.PI * i / 180) * radius;
			Double y = Math.sin(Math.PI * i / 180) * radius;
			vertexBuffer.put(i, x.floatValue());
			vertexBuffer.put(i + 1, y.floatValue());
		}
		vertexBuffer.rewind();
	}

	public void draw(GL10 gl) {
		gl.glVertexPointer(2, gl.GL_FLOAT, 0, vertexBuffer);
		gl.glDrawArrays(gl.GL_TRIANGLE_FAN, 0, 360);
	}
}
