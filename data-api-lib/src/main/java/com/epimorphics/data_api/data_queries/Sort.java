/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

public class Sort {
	
	final Shortname by;
	final boolean upwards;
	
	public Sort(Shortname by, boolean upwards) {
		this.by = by;
		this.upwards = upwards;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Sort && same( (Sort) other );
	}

	private boolean same(Sort other) {
		return by.equals(other.by) && upwards == other.upwards ;
	}
	
	@Override public String toString() {
		return (upwards ? "UP(" : "DOWN(") + by + ")";
	}
	
}