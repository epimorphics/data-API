/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.List;

import com.hp.hpl.jena.shared.BrokenException;

public class Or extends Bool {
	
	public Or(List<Constraint> operands) {
		super(operands);
	}

	@Override public void toSparql(Context cx, String varSuffix) {
		cx.nest();
		int counter = 0;
		for (Constraint x: operands) {
			if (counter > 0) cx.union();
			cx.begin(this);
			x.toSparql(cx, varSuffix);
			cx.end();
			counter += 1;
		}
		cx.unNest();
	}

	@Override public void toFilterBody(Context cx, String varSuffix) {
		throw new BrokenException("OR as a filter body");			
	}
}