/**
 * @file
 */

package sc.fiji.CMP_BIA.segmentation.superpixels;

import ij.ImagePlus;

/**
 * @class SLIC superpixels
 * @version 0.1
 * @date 10/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief This is derivation of SLIC superpixel in 3D for RGB and gray images
 * with several proposed approximations and speedups such as multi-threading, etc. 
 * 
 * @see http://ivrg.epfl.ch/research/superpixels 
 * @see http://rsbweb.nih.gov/ij 
 * 
 */
public class jSLICf extends jSLIC {

	// TODO - check variables used and overided
	
	// clone of original image we work with
	protected ImagePlus image;
	protected int[] imDims;
	// image converted into LAB colour space 
	// dim int[Width][Height][Slices][channels]
	protected int[][][][] img = null;
	// initial regular grid size
	protected int[][][] gridSize;
	// labeling per each image pixel - dim int[Width]
	protected int[][][] labels = null;
	// protected ShortProcessor labels; // it is 7x slower then int[][]
	// minimal distance according the assigned label 
	protected float[][][] distances = null;
	
	
	
	// TODO - extension for 3D
	
	/**
	 * Constructor that sets the input image.
	 * @param im is the input ImagePlus
	 */
	public jSLICf(ImagePlus im) {
		super(im);
	}
	
	@Override
	protected void initInternalVaribales() {

		// Returns the dimensions of this image as a 5 element int array.
		// (width, height, nChannels, nSlices, nFrames)
		int[] dims = image.getDimensions();
		this.imDims = new int[3];
		imDims[0] = dims[0];
		imDims[1] = dims[1]; 
		// stop processing higher dimension then 3D
		if (dims[3]>1 && dims[4]>1) {
			ij.IJ.log("ERROR: Not supported image dimensions!");
			return;
		} else if (dims[3]>1) {
			imDims[2] = dims[3];
		} else if (dims[4]>1) {
			imDims[2] = dims[4];
		}
		
		// init other local variables according selected image 
		labels = new int[imDims[0]][imDims[1]][imDims[2]];
		distances = new float[imDims[0]][imDims[1]][imDims[2]];
				
		convertImage();
	}
	
	
	@Override
	protected void enforceLabelConnectivity() {
		enforceLabelConnectivity(0.1f);
	}
	
	
	/**
	 * 
	 * @param sizeTrashold
	 */
	protected void enforceLabelConnectivity(float sizeTrashold){
//		// compute the component connectivity among the estimated segments
//		ConnectedComponents connect = new ConnectedComponents(labels);
//		connect.computeConnectivity2D(4);
//		labels = connect.getNewLabeling2D();
//		// create look at table for reassignment
//		int[] LUT = new int[connect.getSegmentSizes().length];
		
		// TODO - finish it
		
		// TODO - extend to 3D
		
	}
			
}
