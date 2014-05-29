/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

public class NegatedMultivaluedFilter extends Constraint {
	
	final Filter basis;
	
	public NegatedMultivaluedFilter(Filter basis) {
		this.basis = basis;			
	}

	@Override public void toSparql(Context cx, String varSuffix) {
		cx.comment("NotFilter toSparql", this);
		cx.negateFilter(basis);
	}

	@Override public void toFilterBody(Context cx, String varSuffix) {
		cx.comment("NotFilter toFilterBody", this);
	}

	public void tripleFiltering(Context cx) {
		cx.negateFilter(basis);
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