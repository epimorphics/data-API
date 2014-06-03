/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ.Const;

public class NegatedMultivaluedFilter extends Constraint {
	
	final Filter basis;
	
	public NegatedMultivaluedFilter(Filter basis) {
		this.basis = basis;			
	}

	// old form generated FILTER NOT EXISTS{t, basis filter}
	// and used varSuffix to disambiguate when multi-valued
	public void tripleFiltering(Context cx) {
		SQ.Resource P = new SQ.Resource(basis.a.asProperty());
		SQ.Variable V = new SQ.Variable(basis.a.asVarName());
		SQ.Triple t = new SQ.Triple(Const.item, P, V);
		System.err.println( ">> TODO: nest in the negated filter");
		cx.sq.addNotExists(t);
	}

	@Override public String toString() {
		return "@not(" + basis + ")";
	}

	@Override protected boolean same(Constraint other) {
		return basis.equals(((NegatedMultivaluedFilter) other).basis);
	}

	@Override public Constraint negate() {
		return basis;
	}
}