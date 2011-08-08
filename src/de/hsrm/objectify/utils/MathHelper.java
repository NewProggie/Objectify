package de.hsrm.objectify.utils;

import Jama.Matrix;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;

public class MathHelper {
	
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
		
		for (int i=0; i<imageHeight; i++) {
			for (int j=0; j<imageWidth; j++) {
				zRealPart[i][j] = zComplex[i][j*2];
			}
		}
				
		return zRealPart;
	}
	

}
