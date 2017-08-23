/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Set;

import org.apache.jena.sparql.util.FmtUtils;

public class SQ_Literal extends SQ_Node {

	final String spelling;
	final String type;
	
	public SQ_Literal(String spelling, String type) {
		this.spelling = spelling;
		this.type = type;
	}
	
	public String spelling() {
		return spelling;
	}
	
	public String type() {
		return type;
	}
	
	protected String safeSpelling() {
		return FmtUtils.stringEsc(spelling, true);
	}
	
	@Override public void toSparqlExpr(StringBuilder sb) {
		sb.append("\"").append(safeSpelling()).append("\"");
		if (!type.isEmpty()) sb.append("^^<").append(type).append(">");
		sb.append(" ");
	}

	@Override public void updateVars(Set<String> varNames) {
		// A literal is not a variable.
	}
}