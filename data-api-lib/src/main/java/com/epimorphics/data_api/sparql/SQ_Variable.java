/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

public class SQ_Variable extends SQ_Node {
	
	final String name;
	
	public SQ_Variable(String name) {
		this.name = name;
	}
	
	public String name() {
		return name;
	}
	
	public String asVar() {
		return "?" + name;
	}

	@Override public void toSparqlExpr(StringBuilder sb) {
		sb.append("?").append(name()).append(" ");
	}
	
}