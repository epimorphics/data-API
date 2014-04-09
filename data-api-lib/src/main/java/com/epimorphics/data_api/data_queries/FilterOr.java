/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.hp.hpl.jena.shared.BrokenException;

public final class FilterOr extends Constraint {
	
	final Constraint A, B;
	
	public FilterOr(Constraint A, Constraint B) {
		this.A = A;
		this.B = B;
	}
	
	@Override public void toSparql(Context cx) {
		cx.comment("SmallOr", A, B);
		cx.out.append( "  FILTER(" );
		A.toFilterBody(cx);
		cx.out.append(" || ");
		B.toFilterBody(cx);
		cx.out.append(")\n");
	}

	@Override public String toString() {
		return "(" + A + " || " + B + ")";
	}

	@Override protected boolean same(Constraint other) {
		FilterOr o = (FilterOr) other;
		return A.equals(o.A) && B.equals(o.B);
	}

	@Override public void toFilterBody(Context cx) {
		throw new BrokenException("SnallOr as a filter body");
	}
}