/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.reporting.Problems;
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
	
	@Override public void topLevelSparql(Problems p, Context cx) {
		cx.out.append("SELECT ?item").append(nl);
		for (Aspect a: cx.ordered) {
			cx.out.append("  ").append(a.asVar()).append(nl);
		}
		cx.out.append("WHERE {").append(nl);
		String union = "";
		for (Constraint c: operands) {
			cx.out.append(union); union = " UNION "; cx.out.append(nl);
			cx.out.append("{");
			c.topLevelSparql(p, cx);
			cx.out.append("}");
		}
		cx.out.append("}");
	}

	@Override public void toFilterBody(Context cx, String varSuffix) {
		throw new BrokenException("OR as a filter body");			
	}
	
	@Override public Constraint negate() {
		List<Constraint> newOperands = new ArrayList<Constraint>();
		for (Constraint y: operands) newOperands.add(y.negate());
		return and(newOperands);
	}
}