/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Set;

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
	
	@Override public String toString() {
		return asVar();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof SQ_Variable && same( (SQ_Variable) other );
	}
	private boolean same(SQ_Variable other) {
		return name.equals(other.name);
	}

	@Override public int hashCode() {
		return name.hashCode();
	}

	@Override public void toSparqlExpr(StringBuilder sb) {
		sb.append("?").append(name());
	}

	@Override public void updateVars(Set<String> varNames) {
		varNames.add(asVar());
	}
	
}