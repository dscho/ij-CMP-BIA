/**
 * @file
 */

package sc.fiji.CMP_BIA.segmentation;


import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.segmentation.superpixels.jSLIC;
import sc.fiji.CMP_BIA.segmentation.tools.Descriptors2D;
import sc.fiji.CMP_BIA.tools.MatrixTools;
import ij.ImagePlus;

/**
 * @class Simultaneous segmentation
 * @version 0.1
 * @date 10/08/2013
 * @author ...
 * @category image segmentation
 * 
 * @brief ...
 */
public class SimultaniousSegentation {
	// reference image
	protected ImagePlus imgRef;
	// moving image
	protected ImagePlus imgMove;
	// segment param
	protected int nbClasses;
	// param for SLIC
	protected int spSize;
	protected float spRegul;
	
	// the instances of superpixels segmentations
	protected jSLIC spRef = null;
	protected jSLIC spMove = null;
	// the list of features for each element
	protected float[][] descRef = null;
	protected float[][] descMove = null;
	// coefficient a_fg
	protected double[][] coefA_Ref = null;
	protected double[][] coefA_Move = null;
	// soft assignment according computed descriptors and classifier coef.
	protected float[][] softSeg_Ref = null;
	protected float[][] softSeg_Move = null;
	// LUT for relabelling segments in initial SLIC segmentations
	protected int[] hardSegm_Ref = null;
	protected int[] hardSegm_Move = null;
	
	/**
	 * class construction and input iamges assignement 
	 * 
	 * @param imR is the reference image of ImagePlus
	 * @param imM is the moving image of ImagePlus
	 */
	public SimultaniousSegentation(ImagePlus imR, ImagePlus imM) {
		this.imgRef = imR;
		this.imgMove = imM;
	}
	
	/**
	 * Compute the superpixel clustering on both input images
	 * 
	 * @param sp
	 * @param img
	 */
	protected void computeSuperpixels(jSLIC sp, ImagePlus img) {
		// SLIC segmentation
		ij.IJ.log("SLIC initialisation...");
		sp = new jSLIC(img);		
		ij.IJ.log("SLIC processing...");
		sp.process(spSize, spRegul);
		ij.IJ.log("SLIC finished.");
		sp.getSegmentation().showOverlapLabeling(img, 0.5);
	}
	
	/**
	 * compute the features on segmented image plus the constant 1.
	 * 
	 * @param sp is instance of superpixel segmentation
	 * @param img is the image which was segmented before
	 * @return float[nbElements][nbFeatures+1] is the final descriptor vector with the 1 on the first position
	 */
	protected float[][] computeDescriptors(jSLIC sp, ImagePlus img) {
		// compute descriptors
		Descriptors2D desc = new Descriptors2D(img, sp.getSegmentation());
		desc.addConstatnt(1);
		desc.computeColourMeanRGB();
		//desc.computeTextureWaveletsHaar(3);		
		//descRef.show();
		
		return desc.getDescMatrix();
	}
	
	/**
	 * Do the N random initialisation to avoid local minimal
	 */
	protected void initSoftMax() {
		
		// TODO
		
	}
	
	/**
	 * 
	 * @return
	 */
	protected double initSingleSoftMax() {
		// TODO
		
		return 0.;
	}
	
	/**
	 * own minimisation process by LBFGS optimizer
	 */
	protected void runSoftMax() {
		
		// TODO
		
	}
	
	/**
	 * The main process
	 * 
	 * @param nbCls
	 * @param size
	 * @param regul
	 */
	public void process(int nbCls, int size, float regul) {
		
		nbClasses = nbCls;
		spSize = size;
		spRegul = regul;

		computeSuperpixels(spRef, imgRef);
		computeSuperpixels(spMove, imgMove);
		
		descRef = computeDescriptors(spRef, imgRef);
		descMove = computeDescriptors(spMove, imgMove);
		
		initSoftMax();
		
		runSoftMax();

		// compute the soft segmentation
		softSeg_Ref = softSegment(softSeg_Ref, descRef);
		softSeg_Move = softSegment(softSeg_Ref, descRef);

		// compute the hard segmentation according the soft segmentations
		hardSegm_Ref = hardSegment(softSeg_Ref);
		hardSegm_Move = hardSegment(softSeg_Move);		
	}
	
	/**
	 * Compute the soft assignment according computed descriptors and classifier coefficients
	 * 
	 * @param coefA is a list coefficients for soft max assignment
	 * @param desc is a list of all descriptors of size float[nbElements][nbDescriptors+1]
	 * @return float[nbElements][nbClasses] which represents the probability for assignment an element to a class
	 */
	protected float[][] softSegment(float[][] coefA, float[][] desc) {
		// dimension check
		assert (coefA.length == nbClasses);
		assert (coefA[0].length == desc[0].length);
		
		// create the soft segmentation LUT
		float[][] seg = new float[desc.length][nbClasses];
		
		// computing the sift segmentation 
		for (int i = 0; i < seg.length; i++) {
			for (int j = 0; j < nbClasses; j++) {
				seg[i][j] = (float) Math.exp( MatrixTools.vectorDotProduct(coefA[j], desc[i]) );
			}
		}		
		float[] softSum = MatrixTools.matrixSumInRows(seg);
		seg = MatrixTools.normMatrix(seg, softSum);
		
		return seg;
	}
	
	/**
	 * Find best label by max probability for each element
	 * 
	 * @param softSeg is a matrix of float[nbElements][nbClasses] which represents the probability for assignment an element to a class 
	 * @return int[nbElements] which is basicaly LUT for relabelling SLIC segmentation
	 */
	protected int[] hardSegment(float[][] softSeg) {
		assert(softSeg != null);
		
		// find best label by max probab. for each elem.
		return MatrixTools.matrixMaxInRows(softSeg);
	}
	
	/**
	 * According the hard segmentations it create new segmentations
	 * 
	 * @param ref is new instance of the segmented ref. Image
	 * @param move is new instance of the segmented moving Image
	 */
	public void getSegmentations(Labelling2D ref, Labelling2D move) {
		assert (spRef != null);
		assert (spMove != null);
		assert (hardSegm_Ref != null);
		assert (hardSegm_Move != null);

		ref = (Labelling2D) spRef.getSegmentation().clone();
		ref.reLabel(hardSegm_Ref);
		
		move = (Labelling2D) spMove.getSegmentation().clone();
		move.reLabel(hardSegm_Move);
	}
	
}