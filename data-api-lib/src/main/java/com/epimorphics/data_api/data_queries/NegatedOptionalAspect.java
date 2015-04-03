/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.List;
import java.util.ArrayList;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.sparql.*;

public final class NegatedOptionalAspect extends Restriction  {
	
	final Filter negated;
	
	public NegatedOptionalAspect(Filter negated) {
		this.negated = negated;
	}

	@Override void applyTo(State s) {
		List<Term> terms = negated.range.operands;
		List<SQ_Expr> operands = new ArrayList<SQ_Expr>(terms.size());
		for (Term t: terms) operands.add(new SQ_TermAsNode(s.cx.api.getPrefixes(), t));
		SQ_Filter negFilter = new SQ_Filter(negated.range.op, negated.a, operands);			
		SQ_Expr bound = new SQ_Call("BOUND", BunchLib.list((SQ_Expr) new SQ_Variable(negated.a.asVar().substring(1))));
		SQ_Expr toAdd = new SQ_Infix(negFilter, "||", new SQ_Call("!", BunchLib.list(bound)));
		s.cx.sq.addSqFilter(toAdd);
	}

	@Override public String toString() {
		return "(" + negated + " | UNBOUND(" + negated.a + ")" + ")";
	}

	@Override protected boolean same(Constraint other) {
		NegatedOptionalAspect o = (NegatedOptionalAspect) other;
		return negated.equals(o.negated);
	}

	@Override public Constraint negate() {
		List<Constraint> operands = new ArrayList<Constraint>();
		operands.add(negated.negate());
		return and(operands);
	}

	@Override protected boolean constrains(Aspect a) {
		return negated.constrains(a);
	}	
}