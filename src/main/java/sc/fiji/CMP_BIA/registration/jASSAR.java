package sc.fiji.CMP_BIA.registration;


import sc.fiji.CMP_BIA.segmentation.SimultaniousSegentation;
import ij.ImagePlus;

/**
 * @class Automatic Simultaneous Segmentation and fast Registration
 * @version 0.1
 * @date 10/06/2013
 * @author ...
 * @category image registration
 * 
 * @brief ...
 * 
 * @detail We describe an automatic method for fast registration of images with 
 * very different appearance. The images are first jointly segmented into 
 * a small number of classes and then the segmented images are registered.
 * 
 * The segmentation calculates feature vectors on superpixels and then it finds 
 * a softmax classifier maximizing mutual information between class labels in 
 * the two images. The registration considers only a~sparse set of rectangular 
 * neighborhoods on the interfaces between classes. A~triangulation is created 
 * with spatial regularization handled by pairwise spring-like terms on the edges. 
 * The optimal transformation is found globally using loopy belief propagation. 
 * Multiresolution helps to improve speed and robustness. 
 * 
 * Our main application is registering stained histological slices, which are 
 * large and differ both in the local and global appearance. We show that our 
 * method has comparable accuracy to standard mutual information based 
 * registration, while being faster and more general.
 */
public class jASSAR {
	// reference image
	protected ImagePlus imgRef;
	// moving image
	protected ImagePlus imgMove;
	//
	protected SimultaniousSegentation seg = null; 

	/**
	 * 
	 * @param imR
	 * @param imM
	 */
	public jASSAR(ImagePlus imR, ImagePlus imM) {
		
		this.imgRef = imR;
		this.imgMove = imM;
		
		// TODO
	}
	
	public void process() {
		
		seg = new SimultaniousSegentation(imgRef, imgMove);
		
		seg.process(5, 20, 0.2f);
		
		// TODO sim. segmentation using Soft-Max and l-BFGS
		
		// TODO transformation function (maybe new class)
		
		// TODO registration using message passing
		
	}
	
}
