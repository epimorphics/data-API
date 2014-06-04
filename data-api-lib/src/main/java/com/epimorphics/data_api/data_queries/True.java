/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;

public class True extends Constraint {
	
	public static True value = new True();
	
	public True() {
	}

	@Override public String toString() {
		return "True";
	}

	@Override protected boolean same(Constraint other) {
		return true;
	}

	public void tripleFiltering(Context cx) {
		// Nothing needs doing.
	}

	@Override public Constraint negate() {
		return False.value;
	}

	@Override protected boolean constrains(Aspect a) {
		return false;
	}
}