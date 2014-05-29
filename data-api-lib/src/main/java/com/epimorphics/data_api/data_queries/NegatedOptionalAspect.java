/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.shared.BrokenException;

public final class NegatedOptionalAspect extends Constraint {
	
	final Filter negated;
	
	public NegatedOptionalAspect(Filter negated) {
		this.negated = negated;
	}
	
	@Override public void toSparql(Context cx, String varSuffix) {
		cx.comment("NegatedOptionalAspect", negated);
		cx.out.append( "  FILTER(" );
		negated.toFilterBody(cx, varSuffix);
		cx.out.append(" || ");
		cx.out.append(" !BOUND(").append(negated.a.asVar()).append(")");
		cx.out.append(")\n");
	}
	
	public void tripleFiltering(Context cx) {
		toSparql(cx, "");
	}

	@Override public String toString() {
		return "(" + negated + " | UNBOUND(" + negated.a + ")" + ")";
	}

	@Override protected boolean same(Constraint other) {
		NegatedOptionalAspect o = (NegatedOptionalAspect) other;
		return negated.equals(o.negated);
	}

	@Override public void toFilterBody(Context cx, String varSuffix) {
		throw new BrokenException("SmallOr as a filter body");
	}

	@Override public Constraint negate() {
		List<Constraint> operands = new ArrayList<Constraint>();
		operands.add(negated.negate());
		return and(operands);
	}
}