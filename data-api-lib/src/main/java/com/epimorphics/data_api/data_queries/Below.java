/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Below extends Constraint {
	
	final Aspect a;
	final Term v;
	final boolean negated;
	
	public Below(Aspect a, Term v) {
		this(a, v, false);
	}
	
	public Below(Aspect a, Term v, boolean negated) {
		this.a = a;
		this.v = v;
		this.negated = negated;
	}

	public void tripleFiltering(Context cx) {
		cx.comment("@below", this);
			PrefixMapping pm = cx.api.getPrefixes();
		//
			String fVar = this.a.asVar(); 
			String value = this.v.asSparqlTerm(pm);			
			Aspect x = this.a;
			String below = x.getBelowPredicate(cx.api);
		//
			if (negated) cx.out.append("  FILTER(NOT EXISTS{");
			cx.out.append(value)
				.append(" ")
				.append(below)
				.append("* ")
				.append(fVar)
				.append(" .")
				;			
			if (negated) cx.out.append("})");
			cx.out.append("\n");
	}

	@Override public String toString() {
		return a + " @below " + v;
	}

	@Override protected boolean same(Constraint other) {
		return same((Below) other);
	}

	protected boolean same(Below other) {
		return a.getName().equals(other.a.getName()) && v.equals(other.v);
	}

	@Override public Constraint negate() {
		return new Below(a, v, !negated);
	}	
}