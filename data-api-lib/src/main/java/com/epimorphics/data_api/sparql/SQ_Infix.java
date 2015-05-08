/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.List;

import com.epimorphics.data_api.libs.BunchLib;

public class SQ_Infix implements SQ_Expr {
	
	final String op;
	final SQ_Expr L, R;
	
	public SQ_Infix(SQ_Expr L, String op, SQ_Expr R) {
		this.op = op;
		this.L = L;
		this.R = R;
	}
	
	@Override public void toSparqlExpr(StringBuilder sb1) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");		
		L.toSparqlExpr(sb);
		sb.append(" ").append(op).append(" ");
		R.toSparqlExpr(sb);
		sb.append(")");
		sb1.append(sb);
	}

	private String show(SQ_Expr e) {
		StringBuilder sb = new StringBuilder();
		e.toSparqlExpr(sb);
		return sb.toString();
	}

	@Override public List<SQ_Expr> operands() {
		return BunchLib.list(L, R);
	}

}
