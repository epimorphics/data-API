/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.hp.hpl.jena.shared.BrokenException;

public class Below extends Constraint {
	
	final Aspect a;
	final Term v;
	
	public Below(Aspect a, Term v) {
		this.a = a;
		this.v = v;
	}

	@Override public void toSparql(Context cx, String varSuffix) {
		cx.generateBelow(this);			
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

	@Override public void toFilterBody(Context cx, String varSuffix) {
		throw new BrokenException("Below as FilterBody");
	}

	@Override public Constraint negate() {
		throw new BrokenException("cannot negate Below(" + a + ", " + v + ")");
	}	
}