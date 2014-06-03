/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.List;

import com.epimorphics.data_api.data_queries.Operator;

public class SQ_Filter implements SQ_WhereElement {
	
	final List<SQ_Expr> operands;
	final SQ_Variable x;
	final Operator op;
	
	public SQ_Filter(Operator op, SQ_Variable x, List<SQ_Expr> operands) {
		this.op = op;
		this.x = x;
		this.operands = operands;
	}
			
	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent);
		sb.append("FILTER(");
		toStringNoFILTER(sb);
		sb.append(")");
		sb.append(SQ.nl);
	}		
	
	public void toStringNoFILTER(StringBuilder sb) {
		op.asExpression(sb, x, operands);
	}

}