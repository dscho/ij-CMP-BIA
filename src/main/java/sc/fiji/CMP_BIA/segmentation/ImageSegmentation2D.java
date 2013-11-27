/**
 * @file
 */
package sc.fiji.CMP_BIA.segmentation;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling;
import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.segmentation.superpixels.jSLIC;
import sc.fiji.CMP_BIA.segmentation.tools.Descriptors2D;
import ij.ImagePlus;

/**
 * @class Image segmentation
 * @version 0.1
 * @date 18/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief Image segmentation for multi-class segmentation of really large images
 * The segmentation pipeline is composed form following steps:
 * * SLIC superpixel clustering
 * * descriptor/features extraction for each superpixel
 * * fitting by Gaussina Mixture Model (assuming each Gaussina represent one class)
 * * GraphCut segmentation to obtain compact segments
 * 
 */
public class ImageSegmentation2D {
	// segmented image
	protected ImagePlus image;
	// labeling per each image pixel - dim int[Width][Height]
	protected Labelling2D labels = null;
	// instance of SLIC superpixels
	protected jSLIC superpixels;
	// descriptor instance 
	protected Descriptors2D descriptors;
	// superpixel descriptors
	protected float[][] descList;
	// Gaussian Mixture Model
	//protected GaussianMixture GMM;
	

	/**
	 * Default constructor which asks only for the image
	 * 
	 * @param img
	 */
	public ImageSegmentation2D(ImagePlus img) {		
		this.image = img;
		this.superpixels = new jSLIC(image);
	}

	/**
	 * abbreviated version of method for segmentation
	 * @see ImageSegmentation2D#process(int, int, float, float)
	 * 
	 * @param nbClasses is integer defines number of classes
	 * @param gSize is initial SLIC size which should be with respect to 
	 * the size of details in the image
	 */
	public void process(int nbClasses, int gSize) {
		process(nbClasses, gSize, 0.25f, 1.0f);
	}
	
	/**
	 * The own segmentation process is made here
	 * 
	 * @param nbClasses is integer defines number of classes
	 * @param gSize is initial SLIC size which should be with respect to 
	 * the size of details in the image
	 * @param slicRegul is parameter which characterise the regularity of SLIC 
	 * superpixels, the range is (0;1)
	 * @param gcRegul is the regulatisation constant for GraphCut segmentation 
	 * which basically influence the compactness of final segmentation 
	 */
	public void process(int nbClasses, int slicSize, float slicRegul, float gcRegul) {
		
		// SLIC segmentation
		superpixels.process(slicSize, slicRegul);
		labels = superpixels.getSegmentation();
		
//		labels.showLabelling();
		labels.showOverlapLabeling(image, 0.2);
		
		// compute descriptors
		descriptors = new Descriptors2D(image, labels);
		descriptors.computeColourMeanRGB();
		descriptors.computeTextureWaveletsHaar(3);
		descList = descriptors.getDescMatrix();

		descriptors.show();
		
//		ExponentMixtureModel gmm = new ExponentMixtureModel(new MultivariateGaussian(), descList);
//		gmm.initBregmanSoftClustering( nbClasses );
//		gmm.printModel();
		
//		int[] LUT = gmm.MAP();
//		labels.reLabel(LUT);
		
	
		// TODO - GraphCut segmentation
		
	}

	
	/**
	 * Returns the final labelling, integer index of a class that a pixel belongs to
	 * 
	 * @return Labelling2D is the final segmentation
	 */
	public Labelling getSegmentation() {
		return labels;
	}

	/**
	 * Presenting the final segmentation
	 */
	public void show() {
		if (labels != null) {
			// TODO and an ovelap presentation
			labels.showLabelling();
		}
	}

}
