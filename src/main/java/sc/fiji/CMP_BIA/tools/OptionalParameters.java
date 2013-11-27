package sc.fiji.CMP_BIA.tools;

/* Translation of the clsParameters.py into Java

import types

class Parameters:
    """ An object of class Parameters is a collection of parameters.
        Its instantiated as:

        p=Parameters(x)

        where x can be either another object of type Parameters,
        or a dictionary, such as

        p=Parameters({'tol':1e-6, 'startx':(0,0,0)})

        Additional keyword parameters to the constructor include:

        default: a dictionary with default values
        keep:    a list of parameter names to keep. If given, all other
                 parameters are discarded, except those given by
                 'default'
        discard: list of parameters to discard. This is done after
                 'default' and 'keep' are processed.
        override: these parameters are added as the last step

        Once the object is instantiated, the values can be read by
        as 'p.tol' or 'p.startx', they should not be changed this way
        (although this cannot be enforced for types passed by reference)
        For invalid names, KeyError is raised.

        Policy: parameter values should be never changed.

 *  */

import java.util.*;

/**
 * @class MatrixTools
 * @version 0.1
 * @date 24/07/2013
 * @author Jirka Borovec <jiri.borovec@fel.cvut.cz>
 * @category tools
 * 
 * @brief reimplementation of parameter class which makes simpler some basic 
 * operation over optional parameters (e.g. merging, removing, etc)
 * The parameters can have only numeric value (double) and they are saved 
 * under key its name
 * 
 * Policy: keep in mind that two different parameters cannot have the same key 
 * name, otherwise entering the new one you lose the previous for ever
 *
 */
public class OptionalParameters{
	
	// internal storage of all parameters
	protected HashMap<String, Double> params = new HashMap<String,Double>();


	/**
	 * Default empty constructor
	 */
	public OptionalParameters() {}
	
	/**
	 * Constructor with a initial set of parameters
	 * 
	 * @param p is initial set of parameters in Hash format 
	 */
	public OptionalParameters(HashMap<String,Double> p){ 
		setParams(p);		
	}
	
	/**
	 * Adding another set of parameters of bash editing of the actual ones
	 * 
	 * @param p is a set of parameters in Hash format
	 */
	public void setParams(HashMap<String,Double> p) {
		// Add the user-defined parameters 
		if (!p.isEmpty()){
			params.putAll(p); 
		}
	}
	
	/**
	 * Adding another set of parameters of bash editing of the actual ones
	 * 
	 * @param p is OptionalParameters instance of parameters
	 */
	public void setParams(OptionalParameters p) {
		setParams(p.params);
	}

	/**
	 * Adding or editing a parameter of name p and new value d
	 * 
	 * @param p is a string name of the parameter
	 * @param d is new value of this parameter
	 */
	public void putParam(String p, double d) {
		params.put(p, d);
	}
	
	/**
	 * Remove a parameter from the collected set
	 * 
	 * @param p is a string name of removing parameter
	 */
	public void removeParam(String p) {
		params.remove(p);
	}
	
	/**
	 * Returns a value of a specific parameter
	 * 
	 * @param p is a string name of demanded parameter
	 * @return double value of this parameter
	 */
	public double getParam(String p) {
		return params.get(p);
	}
	
	/**
	 * Basically reset of all parameters and keeping only incoming new set
	 * 
	 * @param p is a set of parameters in Hash format
	 */
	public void keepParams(HashMap<String,Double> p) {
		// FIXME - the input should be only list of names which should be kept
		// Add the keep keys
		if (!p.isEmpty()){
			params.clear();
			params.putAll(p);
		}
	}
	
	/**
	 * Remove a set of parameters by given array of they names
	 * 
	 * @param p is a array of string names of parameters which should be removed
	 */
	public void removeParams(String[] p) {
		// Remove the keys in discard
		for(int i=0; i<p.length; i++) {
			params.remove( p[i] ); 
		}
	}
		
	/**
	 * Standard getter for all saved parameters
	 * 
	 * @return Hash of parameters names and its values
	 */
	public HashMap<String, Double> getAllParams() {
		HashMap<String, Double> res = new HashMap<String, Double>(params);
		return res;
	}
	
	/**
	 * visualisation of all parameters and its values
	 */
	public void printParams() {
		for (String key: params.keySet()){
            System.out.println("parameter '" + key + "' has value " + params.get(key).toString() );  
		} 
	}
			
}