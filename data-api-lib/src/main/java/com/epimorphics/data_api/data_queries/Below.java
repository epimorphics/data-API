/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.data_queries.terms.TermComposite;
import com.epimorphics.data_api.sparql.SQ_Const;
import com.epimorphics.data_api.sparql.SQ_Node;
import com.epimorphics.data_api.sparql.SQ_Resource;
import com.epimorphics.data_api.sparql.SQ_Triple;
import com.epimorphics.data_api.sparql.SQ_Variable;
import com.hp.hpl.jena.shared.BrokenException;

public class Below extends Restriction {
	
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
	
	@Override void applyTo(State s) {		
		String below = a.getBelowPredicate(s.cx.api);
		
		SQ_Node S = new SQ_Resource(((TermComposite) v).value);
		SQ_Node P = new SQ_Resource(below + "*");
		SQ_Node O = new SQ_Variable(a.asVarName());
		SQ_Triple t = new SQ_Triple(S, P, O);
		
		SQ_Resource asProperty = new SQ_Resource(a.asProperty());
		SQ_Triple bind = new SQ_Triple(SQ_Const.item, asProperty, O);
		s.cx.sq.addTriple(bind);
		
		if (negated) {
			s.cx.sq.addNotExists(t); 
		} else {
			s.cx.sq.addTriple(t) ;
		}
		
		s.defined = true;
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

	@Override protected boolean constrains(Aspect a) {
		return this.a.equals(a);
	}	
}