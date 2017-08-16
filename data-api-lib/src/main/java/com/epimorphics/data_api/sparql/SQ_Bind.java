/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Set;

public class SQ_Bind implements SQ_WhereElement {

	final SQ_Expr value;
	final SQ_Variable var;
	
	public SQ_Bind(SQ_Expr value, SQ_Variable var) {
		this.value = value;
		this.var = var;
	}
	
	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent).append("BIND(");
		value.toSparqlExpr(sb);	
		sb.append(" AS ");
		var.toSparqlExpr(sb);
		sb.append(")").append(SQ.nl);
	}

	@Override public void updateVars(Set<String> varNames) {
		varNames.add(var.asVar());
	}
	
}