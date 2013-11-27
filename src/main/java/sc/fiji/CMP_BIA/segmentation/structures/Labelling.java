/**
 * @file
 */
package sc.fiji.CMP_BIA.segmentation.structures;

import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @class Labelling
 * @version 0.1
 * @date 10/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief An interface to store general multi-class segmentations in nD space. 
 * All derivations from this interface have implement following abstract methods.
 * 
 */
abstract public class Labelling {
	// segmentation dimension
	int[] dims = null;
	// max label
	int maxLabel = 0;
	// histogram
	int[] hist = null;
	
	/**
	 * return maximal label in the segmentation
	 * @return int
	 */
	public int getMaxLabel() {
		return maxLabel;
	}
	
	@Override
	abstract public Object clone();
	
	/**
	 * return the int[] of actual histogram where the indexes represents 
	 * the labels and values corresponds to the number of such labels
	 * 
	 * @return int[maxLabel+1] of histogram
	 */
	public int[] getLabelHist() {
		return hist.clone();
	}
	
	/**
	 * Recompute histogram of actual labelling
	 * 
	 * @return int[nbLabels] histogram with count for each label
	 */
	abstract public int[] computeHistogram();
	
	/**
	 * returns coordinates of all point belonging to the boundaries among 
	 * different labels in given segmentation  
	 * 
	 * @param neighborhood is matrix int[nbPints][nbDims] describing the 
	 * connectivity around one pixel in given regular grid
	 * @return int[nbElements][nbPixels][nbDims] coordinates of the bordering points
	 */
	abstract public ArrayList<ArrayList<int[]>> findElementsBoundaries(int[][] neighborhood);
	
	/**
	 * show the labelling in suitable way (ImajeJ image)
	 */
	abstract public void showLabelling();
	
	/**
	 * draw the labelling on the input image 
	 * 
	 * @param img input image
	 * @param opticaly specify the transparency of overlaid labeling
	 */
	abstract public void showOverlapLabeling(ImagePlus img, double opticaly);
	
	/**
	 * draw the labelling boundaries on the input image 
	 * 
	 * @param img input image
	 * @param neighborhood is matrix int[nbPints][nbDims] describing the 
	 * connectivity around one pixel in given regular grid
	 * @param colour for colouring the boundaries
	 */
	abstract public void showOverlapContours(ImagePlus img, java.awt.Color colour);
	
	/**
	 * relabel actual labelling according given LUT, assume that the size of LUT 
	 * is equal to the number of labels in actual labelling
	 * 
	 * @param LUT is int[maxLabel+1] which specifies the new labelling
	 */
	abstract public void reLabel(int[] LUT);
	
	/**
	 * returns array of dimensions of the labelling structure, for instance of 
	 * 2D labelling the array is int[2]
	 * 
	 * @return int[] array of dimensions of the labelling structure
	 */
	public int[] getDims() {
		return Arrays.copyOf(dims, dims.length);
	}
	
	/**
	 * print to the console (System.out) the actual histogram
	 * basically after any operation with date the histogram should be updated
	 */
	public void printHistogram() {
		if (hist != null) {
			System.out.println( "Histogram:" );
			// ( Arrays.toString(hist) );
			for (int i=0; i<hist.length; i++) {
				System.out.println( "label "+ Integer.toString(i) +" -> "+ Integer.toString(hist[i]));
			}
		} else { 
			System.out.println( "no histogram available" );
		}	
	}
	
	/**
	 * print to the console (System.out) the actual data
	 */
	abstract public void printData();
	
	/**
	 * export segmentation data into a chosen file
	 * 
	 * @param path is String containing the file path
	 */
	abstract public void exportToFile(String path);

	/**
	 * goes over all pixels and by defined connectivity finds all neighbouring segments
	 * 
	 * @param neighbors defines relative position of neighbouring pixels of size int[connect][2]
	 * @return int[nbSegments][nbNeighbors] is matrix neighbours to each segment
	 */
	abstract public int[][] findSegmentsConnectivity(final int[][] neighbors);
	
	/**
	 * goes over all pixels and finds all points where are 3 and more different classes in  defined connectivity
	 * 
	 * @param neighbors defines relative position of neighbouring pixels 
	 * of size int[connect][2]
	 * @return int[][] is list of all boundary points
	 */
	abstract public int[][] findMultiClassBoundaryPoints(final int[][] neighbors);
		
}
