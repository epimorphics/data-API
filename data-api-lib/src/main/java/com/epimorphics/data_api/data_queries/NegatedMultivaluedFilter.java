/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.sparql.SQ_Const;
import com.epimorphics.data_api.sparql.SQ_Filter;
import com.epimorphics.data_api.sparql.SQ_Resource;
import com.epimorphics.data_api.sparql.SQ_Triple;
import com.epimorphics.data_api.sparql.SQ_Variable;

public class NegatedMultivaluedFilter extends Constraint {
	
	final Filter basis;
	
	public NegatedMultivaluedFilter(Filter basis) {
		this.basis = basis;			
	}

	// old form generated FILTER NOT EXISTS{t, basis filter}
	// and used varSuffix to disambiguate when multi-valued
	public void tripleFiltering(Context cx) {
		
		SQ_Filter f = basis.range.asFilterSQ(basis.a); // TODO expose less
		
		SQ_Resource P = new SQ_Resource(basis.a.asProperty());
		SQ_Variable V = new SQ_Variable(basis.a.asVarName());
		SQ_Triple t = new SQ_Triple(SQ_Const.item, P, V);
		
		cx.sq.addNotExists(t, f);
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

	@Override protected boolean constrains(Aspect a) {
		return basis.constrains(a);
	}
}