/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class And extends Bool {

	public And(List<Constraint> operands) {
		super(rearrange(operands));
	}
	
	@Override public Constraint negate() {
		List<Constraint> newOperands = new ArrayList<Constraint>();
		for (Constraint y: operands) newOperands.add(y.negate());
		return or(newOperands);
	}

	// flatten ANDs. Move searches to the front. TODO efficient!
	private static List<Constraint> rearrange(List<Constraint> operands) {
		List<Constraint> A = new ArrayList<Constraint>();
		List<Constraint> B = new ArrayList<Constraint>();
		for (Constraint x: flattenANDs(operands)) {
			(x instanceof SearchSpec ? A : B).add(x); 				
		}
		List<Constraint> Astar = combineSearches(A);
		Astar.addAll(B);
		return Astar;
	}

	private static List<Constraint> combineSearches(List<Constraint> searches) {
		if (searches.size() < 2) {
			return searches; 
		} else {
			Map<Shortname, SearchSpec> specs = new HashMap<Shortname, SearchSpec>();
			for (Constraint c: searches) {
				SearchSpec s = (SearchSpec) c;
				Shortname aName = s.getAspectName();
				SearchSpec already = specs.get(aName);
				if (already == null) {
					specs.put(aName, s.withExplicitField());
				} else {
					specs.put(aName, already.withAndExplicitField(s));
				}
			}
			ArrayList<Constraint> result = new ArrayList<Constraint>(specs.values());
			return result;
		}			
	}

	private static List<Constraint> flattenANDs(List<Constraint> operands) {
		List<Constraint> result = new ArrayList<Constraint>();
		for (Constraint x: operands) {
			if (x instanceof And) {
				result.addAll( ((And) x).operands );
			} else {
				result.add(x);
			}
		}
		return result;
	}
}