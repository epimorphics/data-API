/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.sparql.SQ_Expr;
import com.epimorphics.data_api.sparql.SQ_Filter;
import com.epimorphics.data_api.sparql.SQ_Node;
import com.hp.hpl.jena.shared.PrefixMapping;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Range {
	
	final Operator op;
	final List<Term> operands;
	
	public Range(Operator op, List<Term> operands ) {
		this.op = op;
		this.operands = operands;
	}
	
	public static Range EQ(Term v) {
		return new Range(Operator.EQ, BunchLib.list(v));
	}
	
	@Override public String toString() {
		return "{" + op + " " + joinStrings(operands) + "}";
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
	
	public SQ_Filter asFilterSQ(PrefixMapping pm, Aspect l, String suffix) {
		List<SQ_Expr> operands = new ArrayList<SQ_Expr>(this.operands.size());		
		for (Term t: this.operands) operands.add(termAsExpr(pm, t));
		return new SQ_Filter(op, l, operands, suffix);		
	}

	public static SQ_Expr termAsExpr(final PrefixMapping pm, final Term term) {
		return new SQ_Node() {

			@Override public String toString() {
				return "(Expr " + term + " [" + term.getClass().getSimpleName() + "])";
			}
			
			@Override public void toSparqlExpr(StringBuilder sb) {
				sb.append(term.asSparqlTerm(pm));
			}

			@Override public void updateVars(Set<String> varNames) {
				// TODO 
				throw new NotImplementedException();
			}};
	}
	
}