/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.json.JSFullWriter;

public class TermBad extends Term {
	
	final Object problematic;
	
	public TermBad(Object problematic) {
		this.problematic = problematic;
	}

	@Override public String toString() {
		return problematic.toString();
	}
	
	@Override public int hashCode() {
		return problematic.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof TermBad && problematic.equals(((TermBad) other).problematic);
	}
	
	@Override public String asSparqlTerm() {
		return "BAD(" + problematic + ")";
	}

	@Override public void writeTo(JSFullWriter out) {
		throw new UnsupportedOperationException("Cannot write bad Term");
	}
}