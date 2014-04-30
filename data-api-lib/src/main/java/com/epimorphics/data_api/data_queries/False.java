/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

public class False extends Constraint {

	public static False value = new False();
	
	public False() {
	}
	
	@Override public void toSparql(Context cx, String varSuffix) {
		cx.comment("False");
		cx.out.append("  FILTER(false)\n" );
	}

	@Override public String toString() {
		return "True";
	}

	@Override protected boolean same(Constraint other) {
		return true;
	}

	@Override public void toFilterBody(Context cx, String varSuffix) {
		cx.out.append("false");
	}

	@Override public Constraint negate() {
		return True.value;
	}
}
