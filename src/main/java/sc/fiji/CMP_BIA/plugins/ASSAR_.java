package sc.fiji.CMP_BIA.plugins;
/**
 * @file
 */

import java.util.Stack;

import sc.fiji.CMP_BIA.gui.dialogASSAR;
import sc.fiji.CMP_BIA.registration.jASSAR;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

/**
 * @class ASSAR plugin
 * @version 0.1
 * @date 10/06/2013
 * @author ...
 * @category image registration
 * 
 * @brief an ImageJ plugin (interface) for Automatic Simultaneous Segmentation and Registration (ASSAR) of large medical images
 * 
 */
public class ASSAR_  implements PlugIn {
	// handler to the image we work with
	protected ImagePlus imageRef, imageMove;
	// ASSAR method instance
	protected jASSAR segReg;


	/**
	 * This method is run when the current image was accepted.
	 */
	@Override
	public void run(String cmdLine) {
		
		// get list of images
		final ImagePlus[] imageList = createImageList();        
		if (imageList.length != 2) {            
			IJ.error("Two (8, 16, 32-bit or RGB Color) images are required"); 
			return;        
		}
		
		// show the dialog
		final dialogASSAR dialog = new dialogASSAR(IJ.getInstance(), imageList);
	 	dialog.showDialog();
	 	
	 	// If canceled
	 	if (dialog.wasCanceled())  	{
	 		dialog.dispose();
    		return;
    	}
    	
	 	// If OK
     	dialog.dispose();    	       
        
		// Source and target image plus
		this.imageRef = imageList[dialog.getNextChoiceIndex()];
		this.imageMove = imageList[dialog.getNextChoiceIndex()];

		// lock input images
		this.imageRef.lock();
		this.imageMove.lock();
		
		segReg = new jASSAR(imageRef, imageMove);
		
		segReg.process();
		
		// TODO
		

		// unlock input images
		this.imageRef.unlock();
		this.imageMove.unlock();
		
	}

	/**
	 * Show the registered image pair
	 */
	private void showRegistration() {

		// TODO
		
	}

	/**
	 * Show the segmented image pair
	 */
	private void showSegmentations() {

		// TODO - maybe use the visualization function from Segmentation plugin while it will be made
		
	}

	
	void showAbout() { 
		// TODO
	}
	
	boolean showDialog() { 
		// TODO
		return true;  
	}

	/**
     * Create a list with the open images in ImageJ that ASSAR can process.
     * @author Ignacio
     *
     * @return array of references to the open images in bUnwarpJ
     */
    private ImagePlus[] createImageList ()   {
       final int[] windowList = WindowManager.getIDList();
       final Stack <ImagePlus> stack = new Stack <ImagePlus>();
       for (int k = 0; ((windowList != null) && (k < windowList.length)); k++) {
          final ImagePlus imp = WindowManager.getImage(windowList[k]);
          final int inputType = imp.getType();

          // Since October 6th, 2008, bUnwarpJ can deal with 8, 16, 32-bit grayscale 
          // and RGB Color images.
          if ((imp.getStackSize() == 1) || (inputType == ImagePlus.GRAY8) || (inputType == ImagePlus.GRAY16) || (inputType == ImagePlus.GRAY32) || (inputType == ImagePlus.COLOR_RGB)) {
             stack.push(imp);
          }
       }
       final ImagePlus[] imageList = new ImagePlus[stack.size()];
       int k = 0;
       while (!stack.isEmpty()) {
          imageList[k++] = (ImagePlus)stack.pop();
       }
       return(imageList);
    }
	
}
