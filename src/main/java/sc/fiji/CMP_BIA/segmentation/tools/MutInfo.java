/**
 * 
 */
package sc.fiji.CMP_BIA.segmentation.tools;

import sc.fiji.CMP_BIA.tools.MatrixTools;


/**
 * @author borovji3
 *
 */
public class MutInfo {
	// FIXME 
	
	final static float eps = (float) 1e-6;
	//
	float[][] P = null;
	//
	float[] Pi=null, Pj=null;
	// 
	float[][] logP = null;

	/**
	 * 
	 * @param p
	 */
	public MutInfo(float[][] p) {
		assert ( Math.abs(MatrixTools.matrixSum(p)-1.)<1e-3 );
		P = MatrixTools.matrixAdd(p, eps/(float)nbElems(p));
		Pi = MatrixTools.matrixSumInDim(P, 0);
		Pj = MatrixTools.matrixSumInDim(P, 1);
		logP = MatrixTools.matrixLog( MatrixTools.matrixDiv(P, MatrixTools.vectorOuter(Pi, Pj)) );
	}
	
	/**
	 * 
	 * @return
	 */
	public double crit() {
//		// FIXME tensor product
		return MatrixTools.matrixTensorDot(P, logP);
	}
	
	/**
	 * 
	 * @return
	 */
	public float[][] grad() {
		return MatrixTools.matrixAdd(logP, -1);
	}
	
	/**
	 * count all elements in potentionally asymmetric matrix
	 * 
	 * @param p i a matrix 
	 * @return int numer of elements
	 */
	public static int nbElems(float[][] p) {
		int nb = 0;
		for (int i = 0; i < p.length; i++) {
			nb += p[0].length;
		}
		return nb;
	}
	
}
