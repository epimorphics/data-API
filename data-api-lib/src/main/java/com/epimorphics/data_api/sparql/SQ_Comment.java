/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Set;

import com.epimorphics.data_api.data_queries.Constraint;

public final class SQ_Comment implements SQ_WhereElement {

	final String comment;
	
	public SQ_Comment(String comment) {
		this.comment = comment.replaceAll("\n", " ");
	}
	
	@Override public String toString() {
		return "#(" + comment + ")";
	}
	
	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent).append("# ").append(comment).append(".").append(Constraint.nl);
	}

	@Override public void updateVars(Set<String> varNames) {
		// it's a comment. It doesn't have any variables.
	}
}