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

	// if true, various tests are omitted so as to get a testable WAR.
	public static final boolean dontTest = true;

	// sort the fields of a JS object when Row is generating them
	public static boolean sorting = false;
	
	// set to true if aspect order respects whether or not constraints matter
	public static boolean checkConstraints = true;

	// if true, property paths are not unpacked and are just written
	// out to, and handled by, SPARQL. If false, then intermediate
	// variables are generated, used, and shared.
	public static final boolean onlyImplicityPropertyPathsWay = false;

	// force seargh properties to be early in the list
	public static final boolean forceSearchProperty = true;

	// if true, replace @oneof[X] with @eq[X].
	public static boolean optimiseOneof = true;
	
	// if true, generated BINDs are moved toward the end of
	// their WHERE-clause.
	public static boolean moveBindsDownwards = true;

	public static String reportSettings() {
		return
			" doPaths: " + (onlyImplicityPropertyPathsWay ? "no" : "yes")
			+ ", optimise oneof: " + (optimiseOneof ? "yes" : "no")
			+ ", move binds: " + (moveBindsDownwards ? "yes" : "no")
			+ ", respect constraints: " + (checkConstraints ? "yes" : "no")
			+ ", push @search item property early: " + (forceSearchProperty ? "yes" : "no")
			+ ", flatten query log entry: " + (flattening ? "yes" : "no")
			+ "."
			;
	}


}
