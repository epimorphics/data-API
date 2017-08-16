/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Set;

public class SQ_Resource extends SQ_Node {

	final String uri;
	
	public SQ_Resource(String uri) {
		this.uri = uri;
	}
	
	public String uri() {
		return uri;
	}
	
	@Override public String toString() {
		return "{uri: " + uri + "}";
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof SQ_Resource && same( (SQ_Resource) other );
	}
	
	private boolean same(SQ_Resource other) {
		return uri.equals(other.uri);
	}

	@Override public int hashCode() {
		return uri.hashCode();
	}
	
	@Override public void toSparqlExpr(StringBuilder sb) {
		if (uri.startsWith("http:") || uri.startsWith("eh:")) {
			// System.err.println(">> TODO: fix this fragile absolute-uri test.");
			sb.append("<").append(uri()).append(">");
		} else {
			sb.append("").append(uri()).append("");
		}
	}

	@Override public void updateVars(Set<String> varNames) {
		// A resource is not a variable.
	}
	
}