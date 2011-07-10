package de.hsrm.objectify.utils;

import Jama.Matrix;
import Jama.SingularValueDecomposition;
import android.util.Log;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class MathHelper {

	/**
	 * Calculates the pseudo inverse Matrix
	 * @param x
	 * @return
	 */
	public static Matrix pinv(Matrix x) {
		if (x.rank() < 1) {
			return null;
		}
		if (x.getColumnDimension() > x.getRowDimension()) {
			return pinv(x.transpose().transpose());
		}
		SingularValueDecomposition svdX = new SingularValueDecomposition(x);
		double[] singularValues = svdX.getSingularValues();
		double tol = Math.max(x.getColumnDimension(), x.getRowDimension())
				* singularValues[0] * 2E-16;
		double[] singularValueReciprocals = new double[singularValues.length];
		for (int i = 0; i < singularValues.length; i++) {
			singularValueReciprocals[i] = Math.abs(singularValues[i]) < tol ? 0
					: (1.0 / singularValues[i]);

		}
		double[][] u = svdX.getU().getArray();
		double[][] v = svdX.getV().getArray();
		int min = Math.min(x.getColumnDimension(), u[0].length);
		double[][] inverse = new double[x.getColumnDimension()][x
				.getRowDimension()];
		for (int i = 0; i < x.getColumnDimension(); i++) {
			for (int j = 0; j < u.length; j++) {
				for (int k = 0; k < min; k++) {
					inverse[i][j] += v[i][k] * singularValueReciprocals[k]
							* u[j][k];
				}
			}
		}
		return new Matrix(inverse);
	}

	private static void printMatrix(double[][] matrix) {
		for (int i=0; i<matrix.length; i++) {
			for (int j=0; j<matrix[0].length; j++) {
				Log.d("printMatrix", String.valueOf(matrix[i][j]));
			}
		}
	}
	
	/**
	 * Calculates height field from given surface gradients.
	 * @param pGradients
	 * @param qGradients
	 * @param imageHeight width of image
	 * @param imageWidth height of image
	 * @return
	 */
	public static double[][] twoDimIntegration(Matrix pGradients, Matrix qGradients, int imageHeight, int imageWidth) {
		int rows = pGradients.getRowDimension();
		int cols = pGradients.getColumnDimension();
		
		DoubleFFT_2D dfft = new DoubleFFT_2D(rows, cols);
		
		double[][] pComplex = new double[rows][cols*2];
		double[][] qComplex = new double[rows][cols*2];
		double[][] zComplex = new double[rows][cols*2];
		double[][] zRealPart = new double[rows][cols];
		
		for (int i=0; i<pGradients.getRowDimension(); i++) {
			for (int j=0; j<pGradients.getColumnDimension(); j++) {
//				Log.d("pGradients(i,j)", String.valueOf(pGradients.get(i, j)));
//				Log.d("qGradients(i,j)", String.valueOf(qGradients.get(i, j)));
				pComplex[i][j] = pGradients.get(i, j);
				qComplex[i][j] = qGradients.get(i, j);
			}
		}
		
		
		dfft.realForwardFull(pComplex);
		dfft.realForwardFull(qComplex);
		
		int idx = 0;
		for (int i=0; i<imageHeight; i++) {
			for (int j=0; j<imageWidth; j++) {
				if ((i != 0) || (j != 0)) {
					double u = Math.sin(i*2*Math.PI / imageHeight);
					double v = Math.sin(j*2*Math.PI / imageWidth);
					double uv = u*u + v*v;
					double d = uv;
					zComplex[i][idx] = (u*pComplex[i][idx+1] + v*qComplex[i][idx+1]) / d;
					zComplex[i][idx+1] = (-u*pComplex[i][idx] - v*qComplex[i][idx]) / d;
				}
				idx += 2;
			}
			idx = 0;
		}
		zComplex[0][0] = 0;
		zComplex[0][1] = 0;
		
		dfft.complexInverse(zComplex, true);
		idx = 0;
		for (int i=0; i<imageHeight; i++) {
			for (int j=0; j<imageWidth; j++) {
				zRealPart[i][j] = zComplex[i][idx];
				idx += 2;
			}
			idx = 0;
		}
		
		return zRealPart;
	}
	

}
