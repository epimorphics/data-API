/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.hp.hpl.jena.shared.BrokenException;

public class False extends Restriction {

	public static False value = new False();
	
	public False() {
	}

	@Override void applyTo(State s) {
		throw new BrokenException("False.doAspect should not be called");
	}
	
	@Override public String toString() {
		return "False";
	}

	@Override protected boolean same(Constraint other) {
		return true;
	}

	@Override public Constraint negate() {
		return True.value;
	}

	@Override protected boolean constrains(Aspect a) {
		return false;
	}
}
