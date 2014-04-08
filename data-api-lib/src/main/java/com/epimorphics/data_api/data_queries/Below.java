/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;

public class Below extends Constraint {
	
	final Aspect a;
	final Term v;
	
	public Below(Aspect a, Term v) {
		this.a = a;
		this.v = v;
	}

	@Override public void toSparql(ToSparqlContext cx) {
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
}