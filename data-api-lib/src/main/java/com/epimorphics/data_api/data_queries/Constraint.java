/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
import com.hp.hpl.jena.shared.BrokenException;

public abstract class Constraint {

	public abstract void toSparql(Context cx);
	
	public abstract void toFilterBody(Context cx);

	public abstract String toString();
	
	@Override public boolean equals(Object other) {
		return this.getClass() == other.getClass() && same((Constraint) other);
	}
	
	protected abstract boolean same(Constraint other);

	public Constraint() {
	}
	
	public static final Constraint EMPTY = new True();
	
	public static Constraint and(List<Constraint> operands) {
		if (operands.size() == 1) return operands.get(0);
		return new And(operands);
	}
	
	public static Constraint or(List<Constraint> operands) {
		if (operands.size() == 1) return operands.get(0);
		return new Or(operands);
	}
	
	public static Constraint not(List<Constraint> operands) {
		return new Not(operands);
	}
	
	public static Constraint filters(List<Constraint> filters) {
		return filters(filters, SearchSpec.none());
	}
	
	public static Constraint filters(List<Constraint> filters, List<SearchSpec> searchPatterns ) {
		List<Constraint> operands = new ArrayList<Constraint>(filters.size());
		// for (Filter f: filters) operands.add(Constraint.wrap(f));
		operands.addAll(filters);
		operands.addAll(searchPatterns);
//		for (SearchSpec s: searchPatterns) operands.add(s);
		return and(operands);
	}

	public static class And extends Bool {

		public And(List<Constraint> operands) {
			super(rearrange(operands));
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
			return A;
		}

		private static List<Constraint> combineSearches(List<Constraint> searches) {
			if (searches.size() < 2) {
				return searches; 
			} else {
				return searches;
//				List<Constraint> combined = new ArrayList<Constraint>();
//				SearchSpec first = (SearchSpec) combined.get(0);
//				Shortname a = first.getAspectName();
//				
//				for (int i = 1; i < searches.size(); i += 1) {
//					if ()
//				}
//				
//				return combined;
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

		@Override public void toSparql(Context cx) {
			for (Constraint x: operands) x.toSparql(cx); 
		}

		@Override public void toFilterBody(Context cx) {
			throw new BrokenException("AND as a filter body");
		}
	}
	
	public static class Or extends Bool {
		
		public Or(List<Constraint> operands) {
			super(operands);
		}

		@Override public void toSparql(Context cx) {
			cx.nest();
			int counter = 0;
			for (Constraint x: operands) {
				if (counter > 0) cx.union();
				cx.begin(this);
				x.toSparql(cx);
				cx.end();
				counter += 1;
			}
			cx.unNest();
		}

		@Override public void toFilterBody(Context cx) {
			throw new BrokenException("OR as a filter body");			
		}
	}
	
	public static class Not extends Bool {
		
		public Not(List<Constraint> operands) {
			super(operands);
		}

		@Override public void toSparql(Context cx) {			
			cx.notImplemented(this);
		}

		@Override public void toFilterBody(Context cx) {
			throw new BrokenException("NOT as a filter body");			
		}
	}
	
	public static Constraint smallOr( Constraint A, Constraint B ) {
		return new FilterOr(A, B);		
	}
	
	// TODO not
	public static Constraint build(List<Constraint> constraints, Map<String, List<Constraint>> compositions) {
		
//		System.err.println( ">> build: filters  " + filters );
//		System.err.println( ">> build: searches " + searchPatterns );			
		
		List<Constraint> ands = compositions.get("@and");
		List<Constraint> ors = compositions.get("@or");
		List<Constraint> nots = compositions.get("@not");
		Constraint fs = and(constraints); // Constraint.filters(filters, searchPatterns);
		
//		System.err.println( ">> ands: " + ands );
//		System.err.println( ">> ors:  " + ors );
//		System.err.println( ">> nots: " + nots );
//		System.err.println( ">> fs:   " + fs );		
	//
		List<Constraint> expanded_ands = new ArrayList<Constraint>(ands);
		if (nots.size() > 0) expanded_ands.add(negate(nots));
		
//		if (filters.size() > 0 || searchPatterns.size() > 0) expanded_ands.add(fs);
		if (constraints.size() > 0) expanded_ands.add(fs);
		
//		System.err.println( ">> expanded_ands: " + expanded_ands );
	//
		List<Constraint> expanded_ors = new ArrayList<Constraint>(ors);
		if (expanded_ands.size() > 0) expanded_ors.add(Constraint.and(expanded_ands));
		Constraint result = Constraint.or(expanded_ors);
		
// MAY NEED		if (result.operands.size() == 0 && result.op.equals(COp.OR)) result = EMPTY;
		
		// System.err.println( ">> built: " + result );
		
		return result;		
	}

	public static Constraint negate(List<Constraint> nots) {
		List<Constraint> x = new ArrayList<Constraint>();
		for (Constraint n: nots) x.add(negate(n));
		return x.size() == 1 ? x.get(0) : or(x);
	}

	private static Constraint negate(Constraint x) {
		if (x instanceof And) {
			throw new UnsupportedOperationException("cannot negate AND yet");
		} else if (x instanceof Or) {
			throw new UnsupportedOperationException("cannot negate OR yet");			
		} else if (x instanceof Not) {
			throw new UnsupportedOperationException("cannot negate NOT yet");			
		} else if (x instanceof Filter) {
			Filter f = (Filter) x;
			return negate(f);
		} 
		throw new BrokenException("Unhandled negate: " + x);
	}

	private static Constraint negate(Filter f) {
		Aspect a = f.a;
		if (a.getIsMultiValued()) {
			throw new BrokenException("Unhandled multi-valued negate: " + f);
		} else if (a.getIsOptional()) {
			Range notR = new Range(f.range.op.negate(), f.range.operands);
			Constraint notF = new Filter(a, notR);
			Constraint unboundA = new Unbound(a);			
			return smallOr(notF, unboundA);			
		} else {
			return new Filter(a, new Range(f.range.op.negate(), f.range.operands));		
		}
	}	

}
