/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Filter {
	
	final Range range;
	final Shortname name;
	
	public Filter(Shortname name, Range range) {
		this.range = range;
		this.name = name;
	}
	
	@Override public String toString() {
		return "<filter " + name + ": " + range;
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

	public Operator getRangeOp() {
		return range.op;

	}

	/**
	    A Filter is pure if it isn't some kind of triple pattern
	    match. For now we test for the two known impure operators
	*/
	public boolean isPure() {
		if (range.op.equals(Operator.SEARCH) || range.op.equals(Operator.BELOW)) return false;
		return true;
	}

	public void asSparqlFilter
		( PrefixMapping pm
		, Filter filter
		, StringBuilder sb
		, String FILTER
		, API_Dataset api
		, List<Aspect> ordered
		, String fVar, String value) {
		range.op.asSparqlFilter
			( pm, filter, sb, FILTER, api, ordered, fVar, value);
	}

}
