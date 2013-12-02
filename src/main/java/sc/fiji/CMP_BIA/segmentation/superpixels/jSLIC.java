/**
 * @file
 */

package sc.fiji.CMP_BIA.segmentation.superpixels;

import ij.IJ;
import ij.ImagePlus;

import java.lang.Math;
import java.util.Arrays;

import sc.fiji.CMP_BIA.segmentation.structures.Labelling2D;
import sc.fiji.CMP_BIA.tools.Logging;
import sc.fiji.CMP_BIA.tools.Threading;
import sc.fiji.CMP_BIA.tools.converters.ConvertImage;


/**
 * @class SLIC superpixels
 * @version 1.0
 * @date 19/06/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category image segmentation
 * 
 * @brief This is SLIC superpixel segmentation for 2D images only mainly 
 * transcription of the original EPFL code for RGB and and gray images 
 * 
 * @see http://ivrg.epfl.ch/research/superpixels 
 * @see http://rsbweb.nih.gov/ij/
 * 
 * @details Superpixels are becoming increasingly popular for use in computer 
 * vision applications. However, there are few algorithms that output a desired 
 * number of regular, compact superpixels with a low computational overhead. 
 * We introduce a novel algorithm called SLIC (Simple Linear Iterative Clustering) 
 * that clusters pixels in the combined five-dimensional color and image plane 
 * space to efficiently generate compact, nearly uniform superpixels. 
 * The simplicity of our approach makes it extremely easy to use - a lone 
 * parameter specifies the number of superpixels - and the efficiency of 
 * the algorithm makes it very practical. Experiments show that our approach 
 * produces superpixels at a lower computational cost while achieving a 
 * segmentation quality equal to or greater than four state-of-the-art methods, 
 * as measured by boundary recall and under-segmentation error. We also 
 * demonstrate the benefits of our superpixel approach in contrast to existing 
 * methods for two tasks in which superpixels have already been shown to increase 
 * performance over pixel-based methods.
 * 
 * References:
 * 
 * [1] Achanta, Radhakrishna, Appu Shaji, Kevin Smith, Aurelien Lucchi, Pascal Fua, and Sabine S??sstrunk. 
 * "Slic superpixels." ??cole Polytechnique Federal de Lausssanne (EPFL), Tech. Rep 149300 (2010).
 */
public class jSLIC {

	// clone of original image we work with
	protected ImagePlus image;
	private int Width, Height;
	// image converted into LAB colour space - dim int[Width][Height][channels]
	protected int[][][] img = null;
	// initial regular grid size
	protected int gridSize;
	// superpixel elasticity in range (0,1)  
	protected float regul;
	// labeling per each image pixel - dim int[Width][Height]
	protected int[][] labels = null;
	// protected ShortProcessor labels; // it is 7x slower then int[][]
	// minimal distance according the assigned label 
	protected float[][] distances = null;
	// number of estimated segments (labels)
	protected int nbLabels;
	// vector of cluster's colours - dim int[nbClusters][channels]
	protected int[][] clusterColour = null;
	// vector of cluster's positions - dim int[nbClusters][positions]
	protected int[][] clusterPosition = null;
	// stopping treshold value in percent of initial error
	protected float errTreshold = 0.1f;
	// according the VLFeat library the regul is in range {0,1}
	protected float factor;
	// number of channel
	protected int nbChannels = 3;
	// precomputed distances
	protected float[] distGrid = null;

	// TODO - avoiding computations with real numbers
	
	/**
	 * Constructor that sets the input image.
	 * 
	 * @param im is the input ImagePlus
	 */
	public jSLIC (ImagePlus im) {
		// clone image locally
		this.image = im;
		
		initInternalVaribales();		
	}
	
	protected void initInternalVaribales() {
		// image sizes
		this.Width = image.getWidth();
		this.Height = image.getHeight();
		
		// init other local variables according selected image 
		labels = new int[Width][Height];
		distances = new float[Width][Height];
		
		Logging.logMsg("SLIC: image convert.");
		
		// (width, height, nChannels, nSlices, nFrames)
		int[] dims = image.getDimensions();
		// process according image dimension 2D / 3D
		if (dims[3]==1 && dims[4]==1) {
			convertImage();
		} else {
			IJ.error("ERROR: Not supported image dimensions!");
		}
	}
	
	
	/**
	 * Initialise the 2D space which means initialise the label array and distances
	 */
	protected void convertImage() {

		switch (image.getType()) {
			// convert image from RGB to CIE LAB colour space]
			case ImagePlus.COLOR_RGB:
				// converting RGB image to LAB
				//this.img = ConvertImage.rgb2cieLAB(image.getProcessor());
				this.img = ConvertImage.rgb2cieLABfast(image.getProcessor());
				//this.nbChannels = 3;
				break;
			// convert the gray images
			case ImagePlus.GRAY8:
			case ImagePlus.GRAY16:
			case ImagePlus.GRAY32:
				// converting Gray image to the same format as LAB but only one channel
				//this.img = ConvertImage.gray2bright(image.getProcessor());
				//this.nbChannels = 1;
				// converting Gray image to the same format as LAB but only one channel
				this.img = ConvertImage.gray2cieLAB(image.getProcessor());
				//this.nbChannels = 3;
				break;
			default:
				Logging.logMsg("ERROR: Not supported colour space!");
				break;
		}
	}
	
	
	/**
	 * Process the whole segmentation process
	 * 
	 * @param grid integer number defining the initial regular grid size
	 * @param reg float defining the superpixel elasticity in range (0,1)  
	 */
	public void process (int grid, float reg) {
		process (grid, reg, 9, 0.1f);
	}
	
	
	/**
	 * Process the whole segmentation process
	 * 
	 * @param grid integer number defining the initial regular grid size
	 * @param reg float defining the superpixel elasticity in range (0,1)  
	 * @param maxIter number of maximal iterations   
	 * @param sizeTrashold says till which size superpixels will by terminated
	 */
	public void process (int grid, float reg, int maxIter, float sizeTrashold) {
		this.gridSize = (grid < 5) ? 5 : grid;
		this.regul = (reg < 0) ? 0 : reg;
		// according the VLFeat library the regul is in range {0,1}
		this.factor = (regul*regul) * (float)(gridSize);
		float err, lastErr = Float.MAX_VALUE;
		
		Logging.logMsg("SLIC: running with gridSize: " + Integer.toString(gridSize) + " regularity " + Float.toString(regul));
		
		initClusters();
		
		float initErr = computeResidualError();
				
		for (int i=0; i<maxIter; i++) {
		
			//assignment();
			//assignmentFast();
			assignmentFastParallel();

			err = computeResidualError();
			Logging.logMsg("SLIC:  iter " + Integer.toString(i+1) + ", inter. distance is " + Float.toString(err));
			
			//update();
			updateFastParallel();

			// show distance function
			// (new ImagePlus("Estimated segmentation", PresentSegmentation.showSegmentation2D(labels))).show();  
			// show partial labeling
			// (new ImagePlus("Estimated distances", PresentSegmentation.showSegmentation2D(distances))).show();  
			
			// STOP criterion, if consecutive errors are smaller then given treshold
			if ( (lastErr-err) < (initErr*errTreshold)) {
				Logging.logMsg("SLIC: terminate with diff error " + (lastErr-err));
				i = maxIter;
			} else {
				lastErr = err;
			}
						
		}
				
		// At the end of the clustering procedure, some ?orphaned? pixels that 
		// do not belong to the same connected component as their cluster center 
		// may remain. To correct for this, such pixels are assigned the label 
		// of the nearest cluster center using a connected components algorithm.
		
		// the original post-processing by authors which relabel by label on top
		Logging.logMsg("SLIC: enforce label connectivity.");
		enforceLabelConnectivity();
		
		Logging.logMsg("SLIC: DONE.");
	}
	
	
	/**
	 * Initialisation of all local variables as well as providing initial 
	 * cluster generating values by positions
	 */
	protected void initClusters () {
		// compute needed number of clusters
		int nbClusters = (int) (Math.ceil((float)Width/(float)gridSize) * Math.ceil((float)Height/(float)gridSize));
		// init arrays
		clusterColour = new int[nbClusters][img[0][0].length];
		clusterPosition = new int[nbClusters][2];
		
		// do initial assignment - assign labels by initial regular grid
		int maxColumn = (int) Math.ceil(Width / (float)gridSize);
		for (int x=0; x<Width; x++ ) {
			for (int y=0; y<Height; y++ ) {

				labels[x][y] = (int) ((y/gridSize)*maxColumn + (x/gridSize));
				
			}
		}

		update();
		distGrid = null;
		
		// OR - The centers are moved to seed locations corresponding to the 
		// lowest gradient position in a 3 ?????? 3 neighborhood. This is done to 
		// avoid centering a superpixel on an edge, and to reduce the chance 
		// of seeding a superpixel with a noisy pixel.
				
	}

	
	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void assignment () {
		int xB, xE, yB, yE;
		float dist, dLAB;
		// temporary variables - differences
		double distLAB, distPos, dx, dy;
		// double dLAB

		// put minimal distances to maximum
		for(float[] subarray : distances) {
			Arrays.fill(subarray, Float.MAX_VALUE);
		}
		
		// cycle over all clusters and compute distances to all pixels in surrounding
		for (int k=0; k<clusterPosition.length; k++) {

			// compute region of interest for given cluster of size 2*gridSize 
			// which is inside the image
			xB = Math.max(0, (int)(clusterPosition[k][0]-gridSize));
			xE = Math.min((int)(clusterPosition[k][0]+gridSize), Width);
			yB = Math.max(0, (int)(clusterPosition[k][1]-gridSize));
			yE = Math.min((int)(clusterPosition[k][1]+gridSize), Height);
			
			// cycle over all pixels in 2*gridSize region
			for (int x=xB; x<xE; x++ ) {
				for (int y=yB; y<yE; y++ ) {

					// compute distance between given point and cluster center
					dx = x-clusterPosition[k][0];
					dy = y-clusterPosition[k][1];
					// compute position distance
					distPos = (dx*dx) + (dy*dy);
					// compute colour distance over all colour channels
					distLAB = 0;
					for (int i=0; i<nbChannels; i++) {
						dLAB = img[x][y][i]-clusterColour[k][i];
						distLAB += dLAB*dLAB;
					}
										
				//	distLAB = (img[x][y][0]-clusterColour[k][0]) * (img[x][y][0]-clusterColour[k][0]);
				//	if (nbChannels == 3) {
				//		distLAB += (img[x][y][1]-clusterColour[k][1]) * (img[x][y][1]-clusterColour[k][1]);
				//		distLAB += (img[x][y][2]-clusterColour[k][2]) * (img[x][y][2]-clusterColour[k][2]);
				//	} else if (nbChannels == 3) {
				//		// to have in sum similar nb as 3 channels
				//		distLAB += distLAB + distLAB;
				//	}
												
					// by SLIC article
					// dist = (float) Math.sqrt(distLAB + (distPos * Math.pow(regul/(float)gridSize, 2)));
					// dist = (float) Math.sqrt(distLAB + (distPos * coef2));
					dist = (float) (distLAB + (distPos * factor));
					// by gSLIC article
					// dist = (float) (Math.sqrt(distLAB) + Math.sqrt(distPos) * (regul/(double)gridSize));
										
					// if actual distance is smaller then the previous give new label 
					if (dist < distances[x][y]) {
						labels[x][y] = k;
						distances[x][y] = dist;
					}					
				}
			}			
		}
		
	}
	
	protected void computeDistGrid() {
		// if grid is not init
		int sz = 2*gridSize +1;
		if (distGrid == null || distGrid.length != sz) {
			Logging.logMsg(" -> pre-computing the distance grid matrix...");
			// if it is not for actual grid size
			distGrid = new float[sz*sz];
			float dx, dy;
			// fill the array
			for (int x=0; x<sz; x++ ) {
				for (int y=0; y<sz; y++ ) {
					dx = x-gridSize+1;
					dy = y-gridSize+1;
					// compute position distance
					//distGrid[x][y] = ((dx*dx) + (dy*dy))  * factor;
					distGrid[x*sz +y] = ((dx*dx) + (dy*dy))  * factor;
				}
			}
		}
	}

	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void assignmentFast () {
		int xB, xE, yB, yE, i, j;
		int sz = 2*gridSize +1;
		float dist, dL, dA, dB;
		// temporary variables - differences
		float distLAB;
		computeDistGrid();
		Logging.logMsg(" -> fast assignement running...");

		// put minimal distances to maximum
		for(float[] subarray : distances) {
			Arrays.fill(subarray, Float.MAX_VALUE);
		}
		
		// cycle over all clusters and compute distances to all pixels in surrounding
		for (int k=0; k<clusterPosition.length; k++) {

			// compute region of interest for given cluster of size 2*gridSize 
			// which is inside the image
			xB = Math.max(0, (int)(clusterPosition[k][0]-gridSize));
			xE = Math.min((int)(clusterPosition[k][0]+gridSize), Width);
			yB = Math.max(0, (int)(clusterPosition[k][1]-gridSize));
			yE = Math.min((int)(clusterPosition[k][1]+gridSize), Height);

			i=clusterPosition[k][0]-xB+gridSize;
								
			// cycle over all pixels in 2*gridSize region
			for (int x=xB; x<xE; x++, i-- ) {

				j=clusterPosition[k][1]-yB+gridSize;
				
				//i = (clusterPosition[k][0]-x+gridSize) * gridSize;
				//i += clusterPosition[k][1]-yB+gridSize;
				
				for (int y=yB; y<yE; y++, j-- ) {

					// faster then the for cycle...
					dL = img[x][y][0]-clusterColour[k][0];
					dA = img[x][y][1]-clusterColour[k][1];
					dB = img[x][y][2]-clusterColour[k][2];
					distLAB = (dL * dL) + (dA * dA) + (dB * dB);
					
					// by SLIC article
					// dist = (float) Math.sqrt(distLAB + (distPos * Math.pow(regul/(float)gridSize, 2)));
					// dist = (float) Math.sqrt(distLAB + (distPos * coef2));
					//dist = distLAB + distGrid[i][j];
					dist = distLAB + distGrid[i*sz +j];
					// by gSLIC article
					// dist = (float) (Math.sqrt(distLAB) + Math.sqrt(distPos) * (regul/(double)gridSize));
										
					// if actual distance is smaller then the previous give new label 
					if (dist < distances[x][y]) {
						labels[x][y] = k;
						distances[x][y] = dist;
					}	
				}
			}			
		}
		
	}
	

	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void assignmentFastParallel () {
		computeDistGrid();
		Logging.logMsg(" -> fast parallel assignement running...");

		// put minimal distances to maximum
		for(float[] subarray : distances) {
			Arrays.fill(subarray, Float.MAX_VALUE);
		}
		
		final ThreadAssignment[] threads = new ThreadAssignment[Threading.nbAvailableThread()];
		int delta = Width / threads.length;
		
		for (int iThread = 0; iThread < threads.length; iThread++) {
			
			// Concurrently run in as many threads as CPUs  
			threads[iThread] = new ThreadAssignment(img, gridSize, distGrid, clusterPosition, clusterColour, distances, labels);
			// for all regular regions
			if (iThread < (threads.length-1)) {
				threads[iThread].setRange(iThread*delta, (iThread+1)*delta, 0, Height);
			// because of a rounding the last has to cover rest of image
			} else {
				threads[iThread].setRange(iThread*delta, Width, 0, Height);
			}
			
		}
		
		Threading.startAndJoin(threads); 
				
	}
	
	/**
	 * Update the cluster centers for a given assignment (colours and positions)
	 */
	protected void update () {
		int k; // segment index (local)
		// reset counting pixels belongs to given clusters
		int nbPixels[] = new int[clusterPosition.length];
		Arrays.fill(nbPixels, 0);
		// reset all previous cluster centers
		for(int[] subarray : clusterColour) {			Arrays.fill(subarray, 0);		}
		for(int[] subarray : clusterPosition) {			Arrays.fill(subarray, 0);		}
		
		// cycle over whole image and by labels add current value to given cluster center
		for (int x=0; x<Width; x++ ) {
			for (int y=0; y<Height; y++ ) {
				k = labels[x][y];
				// over all image channels
				clusterColour[k][0] += img[x][y][0];
				clusterColour[k][1] += img[x][y][1];
				clusterColour[k][2] += img[x][y][2];
				// over all positions
				clusterPosition[k][0] += x;
				clusterPosition[k][1] += y;
				nbPixels[k] ++;
			}
		}
		
		// cycle over all clusters and divide them by nb assigned pixels (get mean)
		for (k=0; k<clusterPosition.length; k++) {
			if (nbPixels[k] == 0) {		continue;	}
			// over all image channels
			clusterColour[k][0] = clusterColour[k][0] / nbPixels[k];
			clusterColour[k][1] = clusterColour[k][1] / nbPixels[k];
			clusterColour[k][2] = clusterColour[k][2] / nbPixels[k];
			// over all positions
			clusterPosition[k][0] = clusterPosition[k][0] / nbPixels[k];
			clusterPosition[k][1] = clusterPosition[k][1] / nbPixels[k];
		}
	}
	

	/**
	 * Assign cluster index to each pixel in image according the given metric
	 */
	protected void updateFastParallel () {
		Logging.logMsg(" -> fast parallel update running...");

		// reset counting pixels belongs to given clusters
		int nbPixels[] = new int[clusterPosition.length];
		Arrays.fill(nbPixels, 0);
		// reset all previous cluster centers
		for(int[] subarray : clusterColour) {			Arrays.fill(subarray, 0);		}
		for(int[] subarray : clusterPosition) {			Arrays.fill(subarray, 0);		}
		
		final ThreadUpdate[] threads = new ThreadUpdate[Threading.nbAvailableThread()];
		int delta = clusterPosition.length / threads.length;
		
		for (int iThread = 0; iThread < threads.length; iThread++) {
			
			// Concurrently run in as many threads as CPUs  
			threads[iThread] = new ThreadUpdate(img, clusterPosition, clusterColour, labels, nbPixels);
			// for all regular regions
			if (iThread < (threads.length-1)) {
				threads[iThread].setRange(iThread*delta, (iThread+1)*delta);
			// because of a rounding the last has to cover rest of image
			} else {
				threads[iThread].setRange(iThread*delta, clusterPosition.length);
			}
			
		}
		
		Threading.startAndJoin(threads); 
				
	}
	/**
	 * Count residual distance to nearest clusters by given metric
	 * 
	 * @return float returns a sum over all distances to nearest cluster
	 */
	protected float computeResidualError () {
		// error metric
		float err = 0;

		// cycle over all distances
		for (int x=0; x<Width; x++ ) {
			for (int y=0; y<Height; y++ ) {
				err += distances[x][y];
			}
		}
		return err;
	}
	

	/**
	 * Enforce Label Connectivity - Modified original code
	 * At the end of the clustering procedure, some ?orphaned? pixels that do 
	 * not belong to the same connected component as their cluster center may 
	 * remain. To correct for this, such pixels are assigned the label of the 
	 * nearest cluster center using a connected components algorithm.
	 * 
	 * 1. finding an adjacent label for each new component at the start
	 * 2. if a certain component is too small, assigning the previously found
	 *    adjacent label to this component, and not incrementing the label.
	 */
	protected void enforceLabelConnectivity() {
		// FIXME no memory efficient
	
		// 4-connectivity
		final int[] dx = {-1,  0,  1,  0};
		final int[] dy = { 0, -1,  0,  1};

		// image size
		int sz = Width*Height;
		// area of initial superpixel
		int SUPSZ = gridSize*gridSize;
		// create new array of labels and fill by -1
		int[][] nlabels = new int[Width][Height];
		for(int[] subarray : nlabels) { Arrays.fill(subarray, -1); }
		// coordinates to run in the image
		int x, y;
		int lab = 0;
		int adjlabel = 0; //adjacent label
		// array of coordinates for all elements in the actual segment
		int[] xvec = new int[sz];
		int[] yvec = new int[sz];
        int count;
		
        // cycle over all pixels in image
		for( int j = 0; j < Height; j++ ) {
			for( int i = 0; i < Width; i++ ) {
				
				if( nlabels[i][j] > -1) { 	continue; 	}
				
				nlabels[i][j] = lab;
				// Start a new segment
				xvec[0] = i;
				yvec[0] = j;
				// Quickly find an adjacent label for use later if needed
				for( int n = 0; n < dx.length; n++ ) {
					x = xvec[0] + dx[n];
					y = yvec[0] + dy[n];
					if( (x >= 0 && x < Width) && (y >= 0 && y < Height) ) {
						if(nlabels[x][y] >= 0) {
							adjlabel = nlabels[x][y];
						}
					}
				}

				count = 1; // segment size
				// region growing method and storing pixels belongs to segment
				for( int c = 0; c < count; c++ ) {
					for( int n = 0; n < dx.length; n++ ) {
						x = xvec[c] + dx[n];
						y = yvec[c] + dy[n];
						// conditions if it is still the same segment
						if( (x >= 0 && x < Width) && (y >= 0 && y < Height) ) {
							if( 0 > nlabels[x][y] && labels[i][j] == labels[x][y] ) {
								xvec[count] = x;
								yvec[count] = y;
								nlabels[x][y] = lab;
								count++;
							}
						}
					}
				}
				// If segment size is less then a limit, assign an
				// adjacent label found before, and decrement label count.
				// shift by 2, which means that it reduces segments 4times smaller
				if(count <= SUPSZ >> 2) {
					for( int c = 0; c < count; c++ ) {
						nlabels[xvec[c]][yvec[c]] = adjlabel;
					}
					lab--;
				}
				lab++;
			}
		}
		this.labels = nlabels;
		this.nbLabels = lab;
	}
	
	
	/**
	 * gives segmentation with segmented indexes
	 * 
	 * @return int[Width][Height] returns indexes of segmented superpixels
	 */
	public Labelling2D getSegmentation() {
		return new Labelling2D(labels);
	}	
	
	
	/**
	 * gives the number of all various labels in segmentation, where the 
	 * max labels are {0,..,(n-1)}
	 * 
	 * @return int number of labels
	 */
	public int getNbLabels() {
		return this.nbLabels;
	}
	
	
	/**
	 * get the converted image in LAB colour space in case of RGB otherwise 
	 * only gray intensity values
	 * 
	 * @return int[Width][Height][channels]
	 */
	public int[][][] getImage() {
		return this.img;
	}
	
}

/**
 * 
 * @author JB
 *
 */
abstract class ThreadParticularImg2D extends Thread {
	// source image
	protected int[][][] img = null;
	// cluster centres
    protected int[][] clusterPosition = null;
    protected int[][] clusterColour = null;
    // labelling
    protected int[][] labels = null;
    	
    { setPriority(Thread.NORM_PRIORITY); }  
                	
    /**
     * initialisation / copy reference to all needed variables 
     * 
     * @param im - image
     * @param cPos - clusters positions
     * @param cClr - cluster colours
     * @param lab - given labelling
     */
    public ThreadParticularImg2D(int[][][] im, int[][] cPos, int[][] cClr, int[][] lab) {
		img = im;
		clusterPosition = cPos;
		clusterColour = cClr;
		labels = lab;
	}
}

/**
 * The particular thread for assignment in given region
 * @author JB
 *
 */
class ThreadAssignment extends ThreadParticularImg2D {  
    // grid size
    protected int gridSize;
    // precomputed distances
    protected float[] distGrid = null;
    // estimated distances
    protected float[][] distances = null;
    // set range
    protected int beginWidth, endWidth, beginHeight, endHeight;
    		
    /**
     * initialisation / copy reference to all needed variables 
     * 
     * @param im - image
     * @param gSize - grid size
     * @param dGrid - recomputed grid
     * @param cPos - clusters positions
     * @param cClr - cluster colours
     * @param dist - the internal distances
     * @param lab - given labelling
     */
    public ThreadAssignment(final int[][][] im, final int gSize, final float[] dGrid, final int[][] cPos, final int[][] cClr, final float[][] dist, int[][] lab) {
		super(im, cPos, cClr, lab);
    	gridSize = gSize;
		distGrid = dGrid;
		distances = dist;
	}
    
    /**
     * setting the particular rectangle in image to be processed
     * 
     * @param sW - start in width dim
     * @param eW - end in width dim
     * @param sH - start in height dim
     * @param eH - end in height dim
     */
    public void setRange(final int bW, final int eW, final int bH, final int eH) {
    	beginWidth = bW;
    	endWidth = eW;
    	beginHeight = bH;
    	endHeight = eH;
	}
    
    /**
     * the main body of the thread
     */
    @Override
    public void run() {  
    	// init
    	int xB, xE, yB, yE, i;
		float dist, dL, dA, dB;
		int sz = 2*gridSize +1;
		// temporary variables - differences
		float distLAB;
		            	
    	// cycle over all clusters and compute distances to all pixels in surrounding
		for (int k=0; k<clusterPosition.length; k++) {

			// compute region of interest for given cluster of size 2*gridSize 
			// which is inside the image
			xB = Math.max(beginWidth, (int)(clusterPosition[k][0]-gridSize));
			xE = Math.min((int)(clusterPosition[k][0]+gridSize), endWidth);
			yB = Math.max(beginHeight, (int)(clusterPosition[k][1]-gridSize));
			yE = Math.min((int)(clusterPosition[k][1]+gridSize), endHeight);

			//i=clusterPosition[k][0]-xB+gridSize;
			
			// cycle over all pixels in 2*gridSize region
			for (int x=xB; x<xE; x++ ) {

				//j=clusterPosition[k][1]-yB+gridSize;
				
				i = (clusterPosition[k][0]-x+gridSize) * sz;
				i += clusterPosition[k][1]-yB+gridSize;
				
				for (int y=yB; y<yE; y++, i-- ) {

					// faster then the for cycle...
					dL = img[x][y][0]-clusterColour[k][0];
					dA = img[x][y][1]-clusterColour[k][1];
					dB = img[x][y][2]-clusterColour[k][2];
					distLAB = (dL * dL) + (dA * dA) + (dB * dB);
					
					// by SLIC article
					// dist = (float) Math.sqrt(distLAB + (distPos * Math.pow(regul/(float)gridSize, 2)));
					// dist = (float) Math.sqrt(distLAB + (distPos * coef2));
					//dist = distLAB + distGrid[i][j];
					dist = distLAB + distGrid[i];
					// by gSLIC article
					// dist = (float) (Math.sqrt(distLAB) + Math.sqrt(distPos) * (regul/(double)gridSize));
										
					// if actual distance is smaller then the previous give new label 
					if (dist < distances[x][y]) {
						labels[x][y] = k;
						distances[x][y] = dist;
					}
				}
			}			
		}
    	
    }
}

/**
 * The particular thread for update in given region
 * @author JB
 */
class ThreadUpdate extends ThreadParticularImg2D {
	// number per cluster
	protected int[] nbPixels = null;
    // set range
    protected int beginK, endK;
	
	
    /**
     * initialisation / copy reference to all needed variables 
     * 
     * @param im - image
     * @param cPos - clusters positions
     * @param cClr - cluster colours
     * @param lab - given labelling
     */
    public ThreadUpdate(final int[][][] im, int[][] cPos, int[][] cClr, final int[][] lab, int[] nb) {
		super(im, cPos, cClr, lab);
    	nbPixels = nb;
	}
    
    public void setRange(final int start, final int stop) {
    	beginK = start;
    	endK = stop;
	}
    
    @Override
    public void run() {
    	int k;
    	// cycle over whole image and by labels add current value to given cluster center
		for (int x=0; x<img.length; x++ ) {
			for (int y=0; y<img[0].length; y++ ) {
				// only for selected K in range
				if (labels[x][y] >= beginK && labels[x][y] < endK) {	
					// save the k
					k = labels[x][y];
					// over all image channels
					clusterColour[k][0] += img[x][y][0];
					clusterColour[k][1] += img[x][y][1];
					clusterColour[k][2] += img[x][y][2];
					// over all positions
					clusterPosition[k][0] += x;
					clusterPosition[k][1] += y;
					nbPixels[k] ++;
				}
			}
		}
		
		// cycle over all clusters and divide them by nb assigned pixels (get mean)
		for (k=beginK; k<endK; k++) {
			if (nbPixels[k] == 0) {		continue;	}
			// over all image channels
			clusterColour[k][0] = clusterColour[k][0] / nbPixels[k];
			clusterColour[k][1] = clusterColour[k][1] / nbPixels[k];
			clusterColour[k][2] = clusterColour[k][2] / nbPixels[k];
			// over all positions
			clusterPosition[k][0] = clusterPosition[k][0] / nbPixels[k];
			clusterPosition[k][1] = clusterPosition[k][1] / nbPixels[k];
		}
    	
    }
	
}
