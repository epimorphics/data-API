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

	// if true, various tests are omitted so as to get a testable WAR.
	public static final boolean dontTest = false;

	// sort the fields of a JS object when Row is generating them
	public static boolean sorting = false;

	public static String reportSettings() {
		return "";
	}

}
