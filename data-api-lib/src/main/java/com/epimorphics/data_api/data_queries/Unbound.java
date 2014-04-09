/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.hp.hpl.jena.shared.BrokenException;

public class Unbound extends Constraint {

	final Aspect a;
	
	public Unbound(Aspect a) {
		this.a = a;
	}
	
	@Override public void toSparql(Context cx) {
		throw new BrokenException("UNBOUND as SPARQL");		
	}

	@Override public String toString() {
		return "(Unbound " + a + ")";
	}

	@Override protected boolean same(Constraint other) {
		return a.equals(((Unbound) other).a);
	}

	@Override public void toFilterBody(Context cx) {
		cx.out.append(" !BOUND(");
		cx.out.append(a.asVar());
		cx.out.append(")");	
	}
}