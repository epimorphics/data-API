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

public abstract class Constraint {

	public abstract void toSparql(Context cx, String varSuffix);
	
	public abstract void toFilterBody(Context cx, String varSuffix);

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
		if (operands.size() == 0) return EMPTY;
		return new And(operands);
	}
	
	public static Constraint or(List<Constraint> operands) {
		if (operands.size() == 1) return operands.get(0);
		if (operands.size() == 0) return EMPTY;
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
				( aPattern
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
					( jointPattern
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
//			return A == null ? B : B == null ? A : Math.max(A, B);
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

		@Override public void toSparql(Context cx, String varSuffix) {
			for (Constraint x: operands) x.toSparql(cx, varSuffix); 
		}

		@Override public void toFilterBody(Context cx, String varSuffix) {
			throw new BrokenException("AND as a filter body");
		}
	}
	
	public static class Or extends Bool {
		
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
	
	public static class Not extends Bool {
		
		public Not(List<Constraint> operands) {
			super(operands);
		}

		@Override public void toSparql(Context cx, String varSuffix) {			
			cx.notImplemented(this);
		}

		@Override public void toFilterBody(Context cx, String varSuffix) {
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
		return x.size() == 1 ? x.get(0) : and(x);
	}

	private static Constraint negate(Constraint c) {
		if (c instanceof And) {
			And and = (And) c;
			List<Constraint> newOperands = new ArrayList<Constraint>();
			for (Constraint y: and.operands) newOperands.add(negate(y));
			return or(newOperands);
		} else if (c instanceof Or) {
			Or or = (Or) c;
			List<Constraint> newOperands = new ArrayList<Constraint>();
			for (Constraint y: or.operands) newOperands.add(negate(y));
			return and(newOperands);
		} else if (c instanceof Not) {
			throw new UnsupportedOperationException("cannot negate NOT yet");			
		} else if (c instanceof Filter) {
			Filter f = (Filter) c;
			return negate(f);
		} 
		throw new BrokenException("Unhandled negate: " + c);
	}
	
	public static class NotFilter extends Constraint {
		
		final Filter basis;
		
		public NotFilter(Filter basis) {
			this.basis = basis;			
		}

		@Override public void toSparql(Context cx, String varSuffix) {
			cx.comment("NotFilter toSparql", this);
			cx.addMinus(basis);
		}

		@Override public void toFilterBody(Context cx, String varSuffix) {
			cx.comment("NotFilter toFilterBody", this);
		}

		@Override public String toString() {
			return "@not(" + basis + ")";
		}

		@Override protected boolean same(Constraint other) {
			return basis.equals(((NotFilter) other).basis);
		}
	}

	private static Constraint negate(Filter f) {
		Aspect a = f.a;
		if (a.getIsMultiValued()) {
			return new NotFilter(f);			
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
