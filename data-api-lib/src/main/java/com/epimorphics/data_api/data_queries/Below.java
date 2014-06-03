/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.data_queries.terms.TermComposite;
import com.epimorphics.data_api.sparql.SQ;

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
		String below = a.getBelowPredicate(cx.api);
		
		SQ.Node S = new SQ.Resource(((TermComposite) v).value);
		SQ.Node P = new SQ.Resource(below + "*");
		SQ.Node O = new SQ.Variable(a.asVarName());
		SQ.Triple t = new SQ.Triple(S, P, O);
		
		if (negated) cx.sq.addNotExists(t);
		else cx.sq.addTriple(t);
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