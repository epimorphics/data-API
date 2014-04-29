/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

public class NotFilter extends Constraint {
	
	final Filter basis;
	
	public NotFilter(Filter basis) {
		this.basis = basis;			
	}

	@Override public void toSparql(Context cx, String varSuffix) {
		cx.comment("NotFilter toSparql", this);
		cx.addMinus(basis);
	}

	@Override public void toFilterBody(Context cx, String varSuffix) {
		cx.comment("NotFilter toFilterBody", this);
	}

	@Override public String toString() {
		return "@not(" + basis + ")";
	}

	@Override protected boolean same(Constraint other) {
		return basis.equals(((NotFilter) other).basis);
	}
}