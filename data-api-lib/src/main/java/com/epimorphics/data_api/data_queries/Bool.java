/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.List;

public abstract class Bool extends Constraint {
	
	public final List<Constraint> operands;

	public Bool(List<Constraint> operands ) {
		this.operands = operands;
	}
	
	@Override public abstract void toSparql(Context cx, String varSuffix);
	
	@Override protected boolean same(Constraint other) {
		List<Constraint> otherOperands = ((Bool) other).operands;
		return operands.equals(otherOperands);
	}
	
	@Override public String toString() {
		return "(" + getClass().getName() + " " + operands + ")";
	}
}