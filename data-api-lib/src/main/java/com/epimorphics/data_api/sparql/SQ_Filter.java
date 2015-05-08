/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Operator;
import com.hp.hpl.jena.shared.BrokenException;

public class SQ_Filter implements SQ_Expr, SQ_WhereElement {
	
	final List<SQ_Expr> operands;
	final Aspect x;
	final Operator op;
	final String suffix;
	
	public SQ_Filter(Operator op, Aspect x, List<SQ_Expr> operands) {
		this(op, x, operands, "");
	}
	
	public SQ_Filter(Operator op, Aspect x, List<SQ_Expr> operands, String suffix) {
		this.op = op;
		this.x = x;
		this.operands = operands;
		this.suffix = suffix;
	}
			
	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent);
		sb.append("FILTER(");
		toStringNoFILTER(sb);
		sb.append(")");
		sb.append(SQ.nl);
	}		
	
	public SQ_Variable aspectAsVariable() {
		return new SQ_Variable(x.asVarName() + suffix);
	}
	
	public void toStringNoFILTER(StringBuilder sb) {
		op.asExpression(sb, aspectAsVariable(), operands);
	}

	@Override public void toSparqlExpr(StringBuilder sb) {
		toStringNoFILTER(sb);
	}

	@Override public List<SQ_Expr> operands() {
		// TODO Auto-generated method stub
		return null;
	}

}