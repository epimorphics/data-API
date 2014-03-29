/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.shared.BrokenException;

public abstract class Composition {
	
	public enum COp { AND, OR, NOT, FILTER, NONE, SEARCH }
	
	public static final Composition EMPTY = new EmptyComposition();
	
	
	static class EmptyComposition extends Composition {

		public EmptyComposition() {
			super(COp.NONE, new ArrayList<Composition>() );
		}

		@Override public void topLevel(Context cx) {
			cx.footPrint( "generated from an EmptyComposition", "");
		}
	}
	
	final COp op;
	final List<Composition> operands;
	
	public interface Context {
		public void footPrint(String message, Object value);
		
		public void generateHead();
		
		public void BEGIN();
		
		public void END();
		
		public void FILTER(Filter f);
	}
	
	public abstract void topLevel(Context cx);
	
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(op);
		for (Composition c: operands) sb.append(" ").append(c.toString());
		sb.append(")");
		return sb.toString();
	}
	
	@Override public boolean equals(Object other) {
		return this.getClass() == other.getClass() && same((Composition) other);
	}
	
	private boolean same(Composition other) {
		return
			this.getClass() == other.getClass()
			&& this.op.equals(other.op)
			&& this.operands.equals(other.operands)
			;
	}
	
	public Composition(COp op, List<Composition> operands) {
		this.op = op;
		this.operands = operands;
	}

	/**
	    A Composition is pure if all its filters are non-triple-filters.
	*/
	public boolean isPure() {
		for (Composition c: operands) if (!c.isPure()) return false;
		return true;
	}

	public boolean isTrivial() {
		boolean result = 
			op.equals(COp.NONE) 
			|| (op.equals(COp.FILTER) && ((FilterWrap) this).f.range.op.equals(Operator.EQ))
			|| (op.equals(COp.AND) && allTrivial())
			;
//		Systesm.err.println( ">> isTrivial " + this + ": " + result );
		return result;
	}
	
	private boolean allTrivial() {
		for (Composition x: operands)
			if (!x.isTrivial()) return false;
		return true;
	}

	public static Composition and(List<Composition> operands) {
		if (operands.size() == 1) return operands.get(0);
		return new And(operands);
	}
	
	public static Composition or(List<Composition> operands) {
		if (operands.size() == 1) return operands.get(0);
		return new Or(operands);
	}
	
	public static Composition not(List<Composition> operands) {
		return new Not(operands);
	}
	
	public static Composition filters(List<Filter> filters) {
		return filters(filters, SearchSpec.none());
	}
	
	public static Composition filters(List<Filter> filters, List<SearchSpec> searchPatterns ) {
		List<Composition> operands = new ArrayList<Composition>(filters.size());
		for (Filter f: filters) operands.add(new FilterWrap(f));	
		for (SearchSpec s: searchPatterns) operands.add(new SearchWrap(s));
		return and(operands);
	}
	
	public static class SearchWrap extends Composition {
		
		final SearchSpec s;
		
		public SearchWrap(SearchSpec s) {
			super(COp.SEARCH, new ArrayList<Composition>() );
			this.s = s;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("(").append(op);
			sb.append(" ").append(s);
			sb.append(")");
			return sb.toString();
		}
		
		/**
		    A SearchSpec is not pure.
		*/
		public boolean isPure() {
			return false;
		}

		@Override public void topLevel(Context cx) {
			cx.footPrint("generated from SearchSpec", s);
		}	
	}
	
	public static class FilterWrap extends Composition {
		
		final Filter f;
		
		public FilterWrap(Filter f) {
			super(COp.FILTER, new ArrayList<Composition>());
			this.f = f;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("(").append(op);
			sb.append(" ").append(f);
			sb.append(")");
			return sb.toString();
		}
		
		/**
		    A Filters is pure if all of its sub-filters are pure.
		*/
		public boolean isPure() {
			return f.isPure();
		}

		@Override public void topLevel(Context cx) {
			cx.footPrint("generated from Filter", f);
			cx.generateHead();
			cx.BEGIN();
			cx.FILTER(f);
			cx.END();
			
			
		}
	}
	
	public static class And extends Composition {

		public And(List<Composition> operands) {
			super(COp.AND, operands);
		}

		@Override public void topLevel(Context cx) {
			cx.footPrint("generated from AND", this);
			cx.generateHead();
			cx.BEGIN();
			cx.END();
		}
	}
	
	public static class Or extends Composition {
		
		public Or(List<Composition> operands) {
			super(COp.OR, operands);
		}

		@Override public void topLevel(Context cx) {
			cx.footPrint("generated from OR", this);
		}
	}
	
	public static class Not extends Composition {
		
		public Not(List<Composition> operands) {
			super(COp.NOT, operands);
		}

		@Override public void topLevel(Context cx) {
			cx.footPrint("generated from NOT", this);
			
		}
	}

	// TODO not
	public static Composition build(List<Filter> filters, List<SearchSpec> searchPatterns, Map<String, List<Composition>> compositions) {
		
		// System.err.println( ">> build: filters " + filters );		
		
		List<Composition> ands = compositions.get("@and");
		List<Composition> ors = compositions.get("@or");
		List<Composition> nots = compositions.get("@not");
		Composition fs = Composition.filters(filters, searchPatterns);
	//
		List<Composition> expanded_ands = new ArrayList<Composition>(ands);
		if (nots.size() > 0) expanded_ands.add(negate(nots));
		if (filters.size() > 0) expanded_ands.add(fs);
	//
		List<Composition> expanded_ors = new ArrayList<Composition>(ors);
		if (expanded_ands.size() > 0) expanded_ors.add(Composition.and(expanded_ands));
		Composition result = Composition.or(expanded_ors);
		
		if (result.operands.size() == 0 && result.op.equals(COp.OR)) result = EMPTY;
		// System.err.println( ">> built: " + result );
		
		return result;		
	}

	public static Composition negate(List<Composition> nots) {
		List<Composition> x = new ArrayList<Composition>();
		for (Composition n: nots) x.add(negate(n));
		return x.size() == 1 ? x.get(0) : or(x);
	}

	private static Composition negate(Composition x) {
		if (x instanceof And) {
			
		} else if (x instanceof Or) {
			
		} else if (x instanceof Not) {
			
		} else if (x instanceof FilterWrap) {
			FilterWrap fs = (FilterWrap) x;
			List<Composition> y = new ArrayList<Composition>();
			return new FilterWrap(negate(fs.f));
		} 
		throw new BrokenException("Cannot negate: " + x);
	}

	private static Filter negate(Filter f) {
		return new Filter(f.name, new Range(f.range.op.negate(), f.range.operands));
	}	

}
