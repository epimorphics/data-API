/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

public class False extends Constraint {

	public static False value = new False();
	
	public False() {
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

	@Override public void tripleFiltering(Context cx) {
		cx.sq.addFalse();
	}
}
