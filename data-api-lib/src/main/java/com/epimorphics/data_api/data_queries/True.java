/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

public class True extends Constraint {
	
	public True() {
	}
	
	@Override public void toSparql(ToSparqlContext cx) {
		cx.comment("True");
	}

	@Override public String toString() {
		return "True";
	}

	@Override protected boolean same(Constraint other) {
		return true;
	}
}