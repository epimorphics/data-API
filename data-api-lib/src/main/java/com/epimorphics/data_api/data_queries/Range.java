/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Operator.InfixOperator;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ.Expr;
import com.epimorphics.data_api.sparql.SQ.FilterSQ;
import com.epimorphics.data_api.sparql.SQ.OpFilter;
import com.epimorphics.data_api.sparql.SQ.Variable;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Range {
	
	final Operator op;
	final List<Term> operands;
	
	public Range(Operator op, List<Term> operands ) {
		
		if (op == null) throw new BrokenException( ">> OOPS OP IS NULL" );
		
		this.op = op;
		this.operands = operands;
	}
	
	public static Range EQ(Term v) {
		return new Range(Operator.EQ, BunchLib.list(v));
	}
	
	@Override public String toString() {
		return "<" + op + " " + joinStrings(operands) + ">";
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Range && same( (Range) other );
	}
	
	private boolean same(Range other) {
		return op.equals(other.op) && operands.equals(other.operands); 
	}

	private String joinStrings(List<Term> operands) {
		StringBuilder sb = new StringBuilder();
		String comma = "";
		for (Term t: operands) {
			sb.append(comma).append(t.toString());
			comma = ", ";
		}
		return sb.toString();
	}

	public FilterSQ asFilterSQ(Aspect a) {
		SQ.Expr l = new SQ.Variable(a.asVarName());
		SQ.Expr r = termAsExpr(operands.get(0));
		
		if (op instanceof InfixOperator) {
			String infix = ((InfixOperator) op).sparqlOp;	
			return new SQ.OpFilter(l, infix, r);
		} else {
			return new SQ.FunFilter(l, op, r);
		}
		
		
	}

	public static Expr termAsExpr(final Term term) {
		final PrefixMapping pm = PrefixMapping.Factory.create();
		return new Expr() {

			@Override public void toString(StringBuilder sb) {
				sb.append(term.asSparqlTerm(pm));
			}};
	}
	
}