package sc.fiji.CMP_BIA.plugins;
/**
 * @file
 */

import java.awt.Color;

import sc.fiji.CMP_BIA.segmentation.ImageSegmentation2D;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
 
/**
 * @class Segmentation plugin
 * @version 0.1
 * @date 18/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief an ImageJ plugin (interface) for segmentation of large medical images
 * 
 * @see http://cmp.felk.cvut.cz/~borovji3/
 */
public class Image_Segmentation_  implements PlugInFilter {
	// handler to the image we work with
	protected ImagePlus image;
	// segmentation paramters
	protected int gSize = 50;
	protected int nbClasses = 4;
	// Segmentation instance
	private ImageSegmentation2D segm;

	/**
	 * This method gets called by ImageJ / Fiji to determine whether the current image is of an appropriate type.
	 *
	 * @param arg can be specified in plugins.config
	 * @param image is the currently opened image
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus img) {
		ij.IJ.log("Image type verification...");
		this.image = img;
		// check that we are working with RGB image
		return DOES_RGB | DOES_8G | DOES_16 | DOES_32;
	}

	/**
	 * This method is run when the current image was accepted.
	 *
	 * @param ip is the current slice
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	@Override
	public void run(ImageProcessor ip) {
		long startTime, estimTime;
		
		// Create interface window
		GenericDialog gd = new GenericDialog("Segmentation");
		gd.addNumericField("Init. grid size: ", this.gSize, 0);
		gd.addNumericField("Number of classes: ", this.nbClasses, 0);

		// show the dialog and quit
		gd.showDialog();
		// if the user clicks "cancel"
		if (gd.wasCanceled()) {		return;		}
		
		// if the user clicks "OK"
		if (gd.wasOKed()) {		
			
			// get values from interface window
			this.gSize = (int) gd.getNextNumber();
			this.nbClasses = (int) gd.getNextNumber();
			this.image.lock();
						
			ij.IJ.log("Segmentation initialisation...");
			startTime = System.currentTimeMillis();
			// init Segmentation
			segm = new ImageSegmentation2D(image);

			ij.IJ.log("Segmentation processing...");
			
			segm.process(nbClasses, gSize);
			
			estimTime = System.currentTimeMillis() - startTime;
			ij.IJ.log("Segmentation process took " + Long.toString(estimTime) + "ms.");
			
			ij.IJ.log("Segmentation visualisation...");
			showSegmentation ();
						
			ij.IJ.log("Segmentation finished.");
			this.image.unlock();
		}
		
	}



	/**
	 * used only for presenting the segmentation results
	 */
	protected void showSegmentation() {

		segm.getSegmentation().showOverlapLabeling(image, 0.5);
		segm.getSegmentation().showOverlapContours(image, Color.RED);
				
	}

	
	void showAbout() { 
		// TODO
	}
	
	boolean showDialog() { 
		// TODO
		return true;  
	}

}

