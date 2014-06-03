/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

public class SQ_Resource extends SQ_Node {

	final String uri;
	
	public SQ_Resource(String uri) {
		this.uri = uri;
	}
	
	public String uri() {
		return uri;
	}
	
	@Override public void toSparqlExpr(StringBuilder sb) {
		if (uri.startsWith("http:") || uri.startsWith("eh:")) {
			System.err.println(">> TODO: fix this fragile absolute-uri test.");
			sb.append("<").append(uri()).append(">").append(" ");
		} else {
			sb.append("").append(uri()).append("").append(" ");
		}
	}
	
}