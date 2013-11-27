/**
 * @file
 */

package sc.fiji.CMP_BIA.classification;

import java.util.ArrayList;
import java.util.Arrays;

import sc.fiji.CMP_BIA.tools.Generators;
import sc.fiji.CMP_BIA.tools.Logging;
import sc.fiji.CMP_BIA.tools.converters.ConvertStructure;


/**
 * @class K-Means
 * @version 1.0
 * @date 12/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category data clustering
 * 
 * @brief implementation of basic K-means clustering generalised to process 
 * all number types (if number is represented as an object), matrix of floats 
 * and PVector (regarding the jMEF library) 
 * 
 * @see http://en.wikipedia.org/wiki/K-means_clustering
 * @see http://vincentfpgarcia.github.io/jMEF/
 */
public class KMeans< T extends Number> {

	// input date of type T and size [nbSamples][nbFeatures]
	protected float[][] data = null;
	// final clusters of type T and size [nbClusters][nbFeatures]
	protected float[][] clusters = null;
	// 
	int nbClusters;
	// distance to nearest cluster or size [nbSamples]
	protected float[] distances = null;
	// assignment of nearest cluster or size [nbSamples]
	protected int[] labels = null;
	//
	private int[] counts = null;


	
//	public KMeans(T[] d) {
//		// check empty data
//		checkEmptyData(d);
//		
//		// clone data into internal structure
//		data = new float[d.length][1];
//		// over all data for summing
//		for(int i=0; i<d.length; i++) {
//			data[i][0] = d[i].floatValue();
//		}
//
//		// init assignments
//		initVariables();
//	}
	
	/**
	 * Constructor which asks only for initial data
	 * 
	 * @param d is matrix Number[nbSamples][nbDataElements] 
	 * representing data to be clustered assuming the fist dimension for number 
	 * of samples and the second for the sample elements
	 */
	public KMeans(T[][] d) {
		// check empty data
		if (d.length < 1) {
			Logging.logMsg("ERROR: empty data!");
			return;
		}
		
		// clone data into internal structure
		data = ConvertStructure.NumberMatrix2floatMatrix(d);
		
		// init assignments
		initVariables();
	}
	
	/**
	 * Constructor which asks only for initial data
	 * 
	 * @param d is matrix float[nbSamples][nbDataElements] 
	 * representing data to be clustered assuming the fist dimension for number 
	 * of samples and the second for the sample elements
	 */
	public KMeans(float[][] d) {
		// check empty data
		if (d.length < 1) {
			Logging.logMsg("ERROR: empty data!");
			return;
		}
		
		// clone data into internal structure
		data = d;
		
		// init assignments
		initVariables();
	}
	
	
//	private void checkEmptyData(T[] d) {
//		// check empty data
//		if (d.length < 1) {
//			System.out.println("ERROR: empty data!");
//			return;
//		}
//	}
	
	/**
	 * Initialisation of local variables such as distances of labelling 
	 */
	protected void initVariables() {
		// init distances
		distances = new float[data.length];
		Arrays.fill(distances, Float.MAX_VALUE);	
		// init assignments
		labels = new int[data.length];
		Arrays.fill(labels, -1);
	}
	

	/**
	 * The main method of KMeans which by given number of demanded clusters 
	 * and maximal number of iterations cluster input data
	 * For initialisation the early clusters are used randomly taken data 
	 * from input data
	 * 
	 * @param nbClusters is int of number of clusters
	 * @param maxIter is a maximal number of iterations if the minimum 
	 * is not reached earlier
	 */
	public void process(int nbClusters, int maxIter) {
		
		float[][] clts = randomClusters(data, nbClusters);
		
		process(clts, maxIter);
		
	}

	/**
	 * The main method of KMeans which by given number of demanded clusters 
	 * and maximal number of iterations cluster input data
	 * 
	 * @param clts is a matrix of Number[nbClusters][nbDataElemnts] which is 
	 * used as initial clusters
	 * @param maxIter is a maximal number of iterations if the minimum 
	 * is not reached earlier
	 */
	public void process(T[][] clts, int maxIter) {
		
		// clone data into internal structure
		float[][] clust = new float[clts.length][clts[0].length];
		// over all data for summing
		for(int i=0; i<clts.length; i++) {
			for (int j=0; j<clts[i].length; j++) {
				clust[i][j] = clts[i][j].floatValue();
			}
		}
		
		process(clust, maxIter);
		
	}
	
	/**
	 * The main method of KMeans which by given number of demanded clusters 
	 * and maximal number of iterations cluster input data
	 * 
	 * @param clts is a matrix of float[nbClusters][nbDataElemnts] which is 
	 * used as initial clusters
	 * @param maxIter is a maximal number of iterations if the minimum 
	 * is not reached earlier
	 */
	public void process(float[][] clts, int maxIter) {
		
		// register clusters
		this.clusters = clts.clone();
		
		// count variable
		nbClusters = clusters.length;
		counts = new int[nbClusters];
				
		// remembering last assignment
		int[] labelsLast = new int[data.length];
		Arrays.fill(labelsLast, -1);
		
		initVariables();
		for (int iter=0; iter<maxIter; iter++) {
			
			// computing distances
			assigne();
			
			// update clusters
			update();

			// print iteration information
			Logging.logMsg("KMeans: inter. distance for iter " + 
					Integer.toString(iter) + "/"+ Integer.toString(maxIter) +
					" is " + Float.toString(sumInterDist()));
			

			// compare with last assignment and if they are same stop iterating
			if (compareAssignments(labelsLast, labels) == 0) {
				iter = maxIter;
				Logging.logMsg("KMeans: termination becase of no changes.");
			} else {
				labelsLast = labels.clone();
			}

			// if empty cluster reinitiate
			if (countEmptyClusters() > 0) {
				int[] emptyClrs = getEmptyClusters();
				
				// to previously empty clusters assign random data samples
				int[] randIdx = Generators.gUniqueRandomIndexes(emptyClrs.length, data.length);
				for (int i=0; i<emptyClrs.length; i++) {
					for (int j=0; j<data[randIdx[i]].length; j++) {
						clusters[emptyClrs[i]][j] = data[randIdx[i]][j];
					}
				}
				
				// update the labeling and compute new centers
				assigne();
				update();

				// TODO - split clusters with largest deviation
			}
			
		}
		
	}
	
	/**
	 * Counting empty clusters. It would be useful in case of random cluster 
	 * initialization where is no guarantee that the init is meaningful
	 * 
	 * @return int number of empty cluster, clusters with zero assigned samples
	 */
	private int countEmptyClusters() {
		int count = 0;
		// go over all clusters
		for (int i=0; i<counts.length; i++) {
			// check empty clusters
			if (counts[i] == 0) {
				count ++;
			}
		}
		return count;
	}
	
	/**
	 * Find all empty clusters meaning clusters with zero assigned samples
	 * 
	 * @return int[nbEmpty] indexes of clusters which does not cover any sample
	 */
	private int[] getEmptyClusters() {
		// if array counts is not defined yet return void array
		if ( counts == null ) {   return null;   }
		// init array of indexes to the empty clusters
		ArrayList<Integer> empty = new ArrayList<Integer>();
		
		// go over all clusters
		for (int i=0; i<counts.length; i++) {
			// check empty clusters
			if (counts[i] == 0) {
				empty.add(i);
			}
		}
		
		return ConvertStructure.arrayList2intArray(empty);
	}
	
	/**
	 * Compare tho different labeling of the same size and return number of 
	 * unequally labeled data in both comparing labeling
	 * 
	 * @param A array int[] of labeling, assuming A.lenght==B.lenght
	 * @param B array int[] of labeling, assuming A.lenght==B.lenght
	 * @return int number of unequally labeled data in both comparing labeling
	 */
	public static int compareAssignments(int[] A, int[] B) {
		int size = A.length;
		// check the array dimensions
		if (A.length != B.length) {
			if (A.length < B.length) {  size = A.length;  }  else  {  size = B.length;  }
			throw new IndexOutOfBoundsException("WARRING: array A has size "+ Integer.toString(A.length)+
												" and array B has size "+ Integer.toString(B.length));
		}
		int diff = 0;
		// go over all elements and count if they are different
		for (int i=0; i<size; i++) {
			if (A[i] != B[i]) {
				diff ++;
			}
		}
		return diff;
	}
	
	
	/**
	 * Sum all minimal internal distances for actual assignment
	 * 
	 * @return total distance
	 */
	private float sumInterDist() {
		float dist = 0;
		for (int i=0; i<distances.length; i++) {
			dist += distances[i];
		}
		return dist;
	}
	
	/**
	 * Random selection of new clusters (initial clusters) from given dataset
	 * 
	 * @param data is matrix of data from which the cluster has to be selected
	 * @param nb is number of selected cluster
	 * @return float[nbClusters][nbDataElements] of randomly taken data samples
	 */
	protected float[][] randomClusters(float[][] data, int nb) {
		
		// get random indexes from data
		int[] rndIdx = Generators.gUniqueRandomIndexes(nb, data.length);
		
		// copy randomly taken samples
		float[][] clts = new float[nb][data[0].length];
		for(int i=0; i<clts.length; i++) {
			for (int j=0; j<data[i].length; j++) {
				clts[i][j] = data[rndIdx[i]][j];
			}
		}
				
		return clts;
	}
	
	/**
	 * Label assignment to all samples according the smallest distance to all 
	 * clusters (both labeling and the smallest distance are stored) 
	 */
	protected void assigne() {
		
		// over all data for summing
		for(int i=0; i<data.length; i++) {
			for (int k=0; k<nbClusters; k++) {
				float sum = 0;
				for (int j=0; j<data[i].length; j++) {
					sum += (float)(clusters[k][j]-data[i][j]) * (float)(clusters[k][j]-data[i][j]);
				}
				// if actual distance is larger then the smallest so far
				if ( sum < distances[i]) {
					distances[i] = sum;
					labels[i] = k;
				}
			}
		}
		
	}
	
	/**
	 * According the labelling minimising the distance the cluster centres are 
	 * computed (updated)
	 */
	protected void update() {
		Arrays.fill(counts, 0);
		// init temporary array for summing features
		float[][] tmp = new float[nbClusters][data[0].length];
		for(float[] subarray : tmp) {   Arrays.fill(subarray, 0);   }
		
		// over all data for summing
		for(int i=0; i<data.length; i++) {
			for (int j=0; j<data[0].length; j++) {
				tmp[labels[i]][j] += data[i][j];
			}
			counts[labels[i]] ++;
		}
		
		// over all cluster for summing
		for(int i=0; i<clusters.length; i++) {
			for (int j=0; j<data[0].length; j++) {
				clusters[i][j] = tmp[i][j] / (float)counts[i];
			}
		}
		
	}
	
	/**
	 * Returns the estimated cluster centres
	 * 
	 * @return float[nbClusters][nbDataElements]
	 */
	public float[][] getClusterCenters() {
		// if not processed yet
		if (clusters == null) {   return null;   }
		
		return clusters.clone();
	}
	
	
	/**
	 * Returns the final labeling
	 * 
	 * @return int[nbSamples] labeling assigning each data to a cluster 
	 */
	public int[] getLabels() {
		return labels.clone();
		
	}
}
