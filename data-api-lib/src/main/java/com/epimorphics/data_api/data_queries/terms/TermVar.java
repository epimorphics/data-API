/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.terms;

import com.epimorphics.json.JSFullWriter;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TermVar extends Term {

	final String name;
	
	public TermVar(String name) {
		this.name = name;
	}

	@Override public String toString() {
		return "<var " + name + ">";
	}
	
	@Override public int hashCode() {
		return name.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof TermVar && name.equals(((TermVar) other).name);
	}
	
	@Override public String asSparqlTerm(PrefixMapping pm) {
		return "?" + name;
	}

	@Override public void writeTo(JSFullWriter out) {
		throw new UnsupportedOperationException("Cannot write variables to JSON.");
	}
}