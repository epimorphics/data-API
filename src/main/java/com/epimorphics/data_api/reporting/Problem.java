/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.reporting;

public class Problem {

	final String message;
	
	public Problem(String message) {
		this.message = message;
	}
	
	public String toText() {
		return message;
	}
	
}