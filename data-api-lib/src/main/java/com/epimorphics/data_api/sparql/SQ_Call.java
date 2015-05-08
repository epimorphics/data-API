/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.List;

public class SQ_Call implements SQ_Expr {

	final String op;
	final List<SQ_Expr> operands;
	
	public SQ_Call(String op, List<SQ_Expr> operands) {
		this.op = op;
		this.operands = operands;
	}
	
	@Override public void toSparqlExpr(StringBuilder sb) {
		sb.append(op).append("(");
		String comma = "";
		for (SQ_Expr arg: operands) {
			sb.append(comma);
			arg.toSparqlExpr(sb);
		}
		sb.append(")");
	}

	@Override public List<SQ_Expr> operands() {
		return operands;
	}

}
