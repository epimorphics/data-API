/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.shared.BrokenException;

public abstract class Constraint {

	public abstract void toSparql(ToSparqlContext cx);

	public abstract String toString();
	
	@Override public boolean equals(Object other) {
		return this.getClass() == other.getClass() && same((Constraint) other);
	}
	
	protected abstract boolean same(Constraint other);

	public Constraint() {
	}
	
	public abstract static class Bool extends Constraint {
		
		final List<Constraint> operands;

		public Bool(List<Constraint> operands ) {
			this.operands = operands;
		}
		
		@Override public abstract void toSparql(ToSparqlContext cx);
		
		@Override protected boolean same(Constraint other) {
			List<Constraint> otherOperands = ((Bool) other).operands;
			return operands.equals(otherOperands);
		}
		
		@Override public String toString() {
			return "(" + getClass().getName() + " " + operands + ")";
		}
	}
	
	public static class True extends Constraint {
		
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
	
	public static Constraint filters(List<Filter> filters) {
		return filters(filters, SearchSpec.none());
	}
	
	public static Constraint filters(List<Filter> filters, List<SearchSpec> searchPatterns ) {
		List<Constraint> operands = new ArrayList<Constraint>(filters.size());
		for (Filter f: filters) operands.add(Constraint.wrap(f));	
		for (SearchSpec s: searchPatterns) operands.add(s);
		return and(operands);
	}
	
	private static Constraint wrap(Filter f) {
		if (f.range.op.equals(Operator.BELOW)) return new Below(f);
		return f;
	}

	public static class Below extends Constraint {
		
		final Filter f;
		
		public Below(Filter f) {
			this.f = f;
		}

		@Override public void toSparql(ToSparqlContext cx) {
			cx.generateBelow(f);			
		}

		@Override public String toString() {
			return f.toString();
		}

		@Override protected boolean same(Constraint other) {
			return same((Below) other);
		}

		protected boolean same(Below other) {
			return f.equals(other.f);
		}	
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
			A.addAll(B);
			return A;
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

		@Override public void toSparql(ToSparqlContext cx) {
			for (Constraint x: operands) x.toSparql(cx); 
		}
	}
	
	public static class Or extends Bool {
		
		public Or(List<Constraint> operands) {
			super(operands);
		}

		@Override public void toSparql(ToSparqlContext cx) {
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
	}
	
	public static class Not extends Bool {
		
		public Not(List<Constraint> operands) {
			super(operands);
		}

		@Override public void toSparql(ToSparqlContext cx) {			
			cx.notImplemented(this);
		}
	}

	// TODO not
	public static Constraint build(List<Filter> filters, List<SearchSpec> searchPatterns, Map<String, List<Constraint>> compositions) {
		
//		System.err.println( ">> build: filters  " + filters );
//		System.err.println( ">> build: searches " + searchPatterns );			
		
		List<Constraint> ands = compositions.get("@and");
		List<Constraint> ors = compositions.get("@or");
		List<Constraint> nots = compositions.get("@not");
		Constraint fs = Constraint.filters(filters, searchPatterns);
		
//		System.err.println( ">> ands: " + ands );
//		System.err.println( ">> ors:  " + ors );
//		System.err.println( ">> nots: " + nots );
//		System.err.println( ">> fs:   " + fs );		
	//
		List<Constraint> expanded_ands = new ArrayList<Constraint>(ands);
		if (nots.size() > 0) expanded_ands.add(negate(nots));
		if (filters.size() > 0 || searchPatterns.size() > 0) expanded_ands.add(fs);
		
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
			throw new UnsupportedOperationException("cannot negate AND");
		} else if (x instanceof Or) {
			throw new UnsupportedOperationException("cannot negate OR");			
		} else if (x instanceof Not) {
			throw new UnsupportedOperationException("cannot negate NOT");			
		} else if (x instanceof Filter) {
			Filter f = (Filter) x;
			return negate(f);
		} 
		throw new BrokenException("Unhandled negate: " + x);
	}

	private static Filter negate(Filter f) {
		return new Filter(f.a, new Range(f.range.op.negate(), f.range.operands));
	}	

}
