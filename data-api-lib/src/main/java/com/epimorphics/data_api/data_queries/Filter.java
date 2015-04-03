/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;

public class Filter extends Restriction {
	
	final Range range;
	final Aspect a;
	
	public Filter(Aspect a, Range range) {
		this.a = a;
		this.range = range;
	}
	
	@Override void applyTo(State s) {
		Term t = range.operands.get(0);
		if (range.op.equals(Operator.EQ) && canReplace(s, t)) {
			s.hasObject(a, t);
		} else {
			s.filter(a, range.op, range.operands);
		}
	}
	
	private boolean canReplace(State s, Term term) {
		Substitution sub = new Substitution(s.getProblems(), this);
		return sub.canReplace;
	}
	
	@Override public Constraint negate() {
		List<Term> operands = range.operands;
		Operator negatedOp = range.op.negate();
	//
		if (a.getIsMultiValued()) {
			return new NegatedMultivaluedFilter(this);			
		} else if (a.getIsOptional()) {
			Range notR = new Range(negatedOp, operands);
			return new NegatedOptionalAspect(new Filter(a, notR));			
		} else {
			return new Filter(a, new Range(negatedOp, operands));		
		}
	}
	
	@Override public String toString() {
		return "<filter " + a.getName() + ": " + range + ">";
	}
	
	@Override public int hashCode() {
		return a.getName().hashCode() ^ range.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Filter && same((Filter) other);
	}
	
	@Override protected boolean same(Constraint other) {
		return same((Filter) other);
	}

	private boolean same(Filter other) {
		return 
			this.a.getName().equals(other.a.getName()) 
			&& this.range.equals(other.range)
			;
	}

	@Override protected boolean constrains(Aspect a) {
		return this.a.equals(a);
	}	
}
