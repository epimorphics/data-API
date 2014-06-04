/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;

public abstract class Bool extends Constraint {
	
	public final List<Constraint> operands;

	public Bool(List<Constraint> operands ) {
		this.operands = operands;
	}
	
	@Override protected boolean same(Constraint other) {
		List<Constraint> otherOperands = ((Bool) other).operands;
		return operands.equals(otherOperands);
	}
	
	@Override public String toString() {
		return "(" + getClass().getName() + " " + operands + ")";
	}

	@Override protected boolean constrains(Aspect a) {
		for (Constraint o: operands) if (o.constrains(a)) return true;
		return false;
	}
}