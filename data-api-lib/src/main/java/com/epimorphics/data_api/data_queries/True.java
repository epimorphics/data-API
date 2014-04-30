/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.hp.hpl.jena.shared.BrokenException;

public class True extends Constraint {
	
	public static True value = new True();
	
	public True() {
	}
	
	@Override public void toSparql(Context cx, String varSuffix) {
		cx.comment("True");
	}

	@Override public String toString() {
		return "True";
	}

	@Override protected boolean same(Constraint other) {
		return true;
	}

	@Override public void toFilterBody(Context cx, String varSuffix) {
		throw new BrokenException("FilterBody of True");
	}

	@Override public Constraint negate() {
		return False.value;
	}
}