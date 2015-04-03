/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
import com.hp.hpl.jena.shared.BrokenException;

public class And extends Bool {

	public And(List<Constraint> operands) {
		super(rearrange(operands));
	}

	void doAspect(State s, Aspect a) {
		throw new BrokenException("And not implemented yet");
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
					specs.put(aName, revise(s));
				} else {
					specs.put(aName, combine(already, s));
				}
			}
			ArrayList<Constraint> result = new ArrayList<Constraint>(specs.values());
			return result;
		}			
	}

	private static SearchSpec revise(SearchSpec s) {
		String aPattern = 
			localName(s.property.URI)
			+ ": "
			+ s.pattern
			;
		return new SearchSpec
			( s.aspect
			, aPattern
			, s.getAspectName()
			, null
			, s.limit
			);
	}

	private static SearchSpec combine(SearchSpec A, SearchSpec B) {
		
		String bField = localName(B.property.URI);
		
		String jointPattern = 
			A.pattern + " "
			+ " AND " + bField + ": " + B.pattern;
			;		
		
		SearchSpec result = new SearchSpec
			( A.aspect
			, jointPattern
			, A.getAspectName()	
			, null
			, max(A.limit, B.limit)
			);
		
		return result;
	}

	private static String localName(String resource) {
		int lastSlash = resource.lastIndexOf('/');
		int lastHash = resource.lastIndexOf('#');
		int begin = Math.max(lastSlash, lastHash);
		return resource.substring(begin + 1);
	}

	private static Integer max(Integer A, Integer B) {
		if (A == null) return B;
		if (B == null) return A;
		return Math.max(A, B);
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