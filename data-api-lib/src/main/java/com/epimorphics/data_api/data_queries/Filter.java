/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

public class Filter {
	
	final Range range;
	final Shortname name;
	
	public Filter(Shortname name, Range range) {
		this.range = range;
		this.name = name;
	}
	
	@Override public int hashCode() {
		return name.hashCode() ^ range.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Filter && same((Filter) other);
	}

	private boolean same(Filter other) {
		return this.name.equals(other.name) && this.range.equals(other.range);
	}

}