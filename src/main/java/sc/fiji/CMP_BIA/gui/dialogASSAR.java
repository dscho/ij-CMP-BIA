/**
 * 
 */
package sc.fiji.CMP_BIA.gui;

import java.awt.Choice;
import java.awt.Frame;
import java.awt.event.ItemEvent;

import ij.ImagePlus;
import ij.gui.GenericDialog;

/**
 * @author borovji3
 *
 */
public class dialogASSAR extends GenericDialog {

	//
	protected int sourceChoiceIndex = 0;
	protected int targetChoiceIndex = 1;
	//
	protected Choice sourceChoice;
	protected Choice targetChoice;

	/** Generated serial version UID */
	private static final long serialVersionUID = 3084303111116185584L;

	public dialogASSAR(final Frame parentWindow,
					final ImagePlus[] imageList) {
		super("ASSAR", null);
		
		// We create a list of image titles to be used as source or target images
		String[] titles = new String[imageList.length];
		for ( int i = 0; i < titles.length; ++i ) {
			titles[i] = imageList[i].getTitle();
		}

		// Source and target choices
		addChoice( "Source_Image", titles, titles[0]);
		this.sourceChoice = (Choice) super.getChoices().lastElement();
		addChoice( "Target_Image", titles, titles[1]);
		this.targetChoice = (Choice) super.getChoices().lastElement();
		
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {

		super.itemStateChanged(e);
		Object o = e.getSource();

		if(!(o instanceof Choice)) {
			return;
		}

		Choice originChoice = (Choice) o;


		// Change in the source image choice
		if(originChoice == this.sourceChoice) {
			final int newChoiceIndex = originChoice.getSelectedIndex();
			if (sourceChoiceIndex != newChoiceIndex) {
				// If the new source image is not the previous target
				if (targetChoiceIndex != newChoiceIndex) {
					sourceChoiceIndex = newChoiceIndex;
				}
				else  { // otherwise, permute 
					targetChoiceIndex = sourceChoiceIndex;
					sourceChoiceIndex = newChoiceIndex;
					this.targetChoice.select(targetChoiceIndex);
				}

			}
		}
		// Change in the target image choice
		else if(originChoice == this.targetChoice) {
			final int newChoiceIndex = originChoice.getSelectedIndex();
			if (targetChoiceIndex != newChoiceIndex) {
				//stopTargetThread();
				// If the new target image is not the previous source
				if (sourceChoiceIndex != newChoiceIndex) {
					targetChoiceIndex = newChoiceIndex;
				}
				else { // otherwise, permute
					//stopSourceThread();
					sourceChoiceIndex = targetChoiceIndex;
					targetChoiceIndex = newChoiceIndex;					
					this.sourceChoice.select(sourceChoiceIndex);
				}
			}
		}
	}

}
