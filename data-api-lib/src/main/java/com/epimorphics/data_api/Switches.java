/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api;

/**
	A place to gather debugging/development toggles and
	values so that they are easy to find and tweak.
	
	They are NOT for programs to tinker with from within.
	They are NOT guaranteed to continue to exist.
*/
public class Switches {

	/**
    	If true, then log entries showing inbound DSAPI queries and 
    	the generated SPARQL queries will be on a single line.  It
    	may be set false when debugging.
	*/
	public static final boolean flattening = true;

	// sort the fields of a JS object when Row is generating them
	public static boolean sorting = false;

	// validate the (type of) terms against the range type of aspects
	// when building a data query.
	public static boolean validatingTermsAgainstTypes = false;

	public static String reportSettings() {
		return
			", flatten query log entry: " + (flattening ? "yes" : "no")
			+ ", validating terms against types: " + (validatingTermsAgainstTypes ? "yes" : "no")
			+ "."
			;
	}
	public static boolean boxing = true;
}
