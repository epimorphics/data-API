/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;

public class Filter {
	
	final Range range;
	final Aspect a;
	
	public Filter(Aspect a, Range range) {
		this.a = a;
		this.range = range;
	}
	
	@Override public String toString() {
		return "<filter " + a.getName() + ": " + range + ">";
	}
	
	@Override public int hashCode() {
		return a.getName().hashCode() ^ range.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Filter && same((Filter) other);
	}

	private boolean same(Filter other) {
		return this.a.getName().equals(other.a.getName()) && this.range.equals(other.range);
	}

	public Operator getRangeOp() {
		return range.op;
	}
}
