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
//
//		@Override public void topLevel(Context cx) {
//			cx.comment( "generated from an EmptyComposition", "");
//		}
//
//		@Override public void asFilter(Context cx) {
//			cx.comment( "generated from an EmptyComposition", "");
////			cx.generateFragment(" true ");
//		}
//
//		@Override public void tripleLevel(Context cx) {
//			cx.comment("tripleLevel EmptyComposition", this);
//		}
//
//		@Override public void translatePureFilter(StringBuilder sb, Context cx) {
//			sb.append(" true ");
//		}
	}
	
	final COp op;
	final List<Composition> operands;
	
	public interface RenderContext {

		void notImplemented(Composition c);
		
		void comment(String message, Object... values);

		void generateFilter(Filter f);
		
		void generateSearch(SearchSpec s);
	}
	
	public static void render(Composition c, RenderContext rx) {
		if (c instanceof And) {
			for (Composition x: c.operands) render(x, rx); 
		} else if (c instanceof Or) {
			rx.notImplemented(c);
		} else if (c instanceof Not) {
			rx.notImplemented(c);
		} else if (c instanceof FilterWrap) {
			FilterWrap w = (FilterWrap) c;
			rx.generateFilter(w.f);
		} else if (c instanceof SearchWrap) {
			SearchWrap s = (SearchWrap) c;
			rx.generateSearch(s.s);
		} else if (c instanceof EmptyComposition) {
			rx.comment("empty composition");
		} else {
			rx.notImplemented(c);
		}
	}
	
//	public interface Context {
//		
//		public void comment(String message, Object value);
//		
//		public void addQueryHead();
//		
//		public void addQueryCore();
//				
//		public void addFilter(Filter f, boolean sayFILTER);
//				
//		public void addSearch(SearchSpec s);
//
//		public void buildPureFilter(StringBuilder sb, Filter f);
//
//		public void topLevelUnion(List<Composition> operands);
//	}
	
//	public abstract void topLevel(Context cx);
//	
//	public abstract void asFilter(Context cx);
//	
//	public abstract void tripleLevel(Context cx);
//
//	public abstract void translatePureFilter(StringBuilder sb, Context cx);
		
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
		
//		/**
//		    A SearchSpec is not pure.
//		*/
//		public boolean isPure() {
//			return false;
//		}

//		@Override public void topLevel(Context cx) {
//			cx.comment("generated from (top-level) SearchSpec", s);
//			cx.addQueryHead();
//			cx.addQueryCore();
//			cx.addSearch(s);
//		}
//
//		@Override public void asFilter(Context cx) {
//			cx.comment("generated from (as filter) SearchSpec", s);
//		}
//
//		@Override public void tripleLevel(Context cx) {
//			cx.comment("generated from (as tripleLevel) SearchSpec", s);
//			cx.addSearch(s);
//		}
//
//		@Override public void translatePureFilter(StringBuilder sb, Context cx) {
//			throw new BrokenException( "@search " + s + " used as pure filter");
//		}	
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

//		public boolean isPure() {
//			return f.isPure();
//		}
//
//		@Override public void topLevel(Context cx) {
//			cx.comment("generated from top-level Filter", f);
//			cx.addQueryHead();
//			cx.addQueryCore();
//			cx.addFilter(f, true);
//		}
//
//		@Override public void asFilter(Context cx) {
//			cx.addFilter(f, false);
//		}
//
//		@Override public void tripleLevel(Context cx) {
//			cx.addFilter(f, true);
//		}
//
//		@Override public void translatePureFilter(StringBuilder sb, Context cx) {
//			cx.buildPureFilter(sb, f);
//		}
	}
	
	public static class And extends Composition {

		public And(List<Composition> operands) {
			super(COp.AND, rearrange(operands));
		}

		// flatten ANDs. Move searches to the front. TODO efficient!
		private static List<Composition> rearrange(List<Composition> operands) {
			List<Composition> A = new ArrayList<Composition>();
			List<Composition> B = new ArrayList<Composition>();
			for (Composition x: flattenANDs(operands)) {
				(x instanceof SearchWrap ? A : B).add(x); 				
			}
			A.addAll(B);
			return A;
		}

		private static List<Composition> flattenANDs(List<Composition> operands) {
			List<Composition> result = new ArrayList<Composition>();
			for (Composition x: operands) {
				if (x instanceof And) {
					result.addAll( x.operands );
				} else {
					result.add(x);
				}
			}
			return result;
		}


//		@Override public void topLevel(Context cx) {
//			cx.comment("generated from top-level AND", this);
//			cx.addQueryHead();
//			cx.addQueryCore();
//			for (Composition x: operands) x.tripleLevel(cx);
//		}
//
//		@Override public void asFilter(Context cx) {
//			for (Composition x: operands) {
//				x.asFilter(cx);
//			}
//		}
//
//		@Override public void tripleLevel(Context cx) {
//			for (Composition x: operands) x.tripleLevel(cx);
//		}
//
//		@Override public void translatePureFilter(StringBuilder sb, Context cx) {
//			sb.append("(");
//			String and = "";
//			for (Composition x: operands) {
//				sb.append(and); and = " && ";
//				x.translatePureFilter(sb, cx);
//			}
//			sb.append(")");			
//		}
	}
	
	public static class Or extends Composition {
		
		public Or(List<Composition> operands) {
			super(COp.OR, operands);
		}

//		@Override public void topLevel(Context cx) {
//			cx.comment("generated from top-level ", this);
//			cx.addQueryHead();
//			cx.topLevelUnion(operands);
//			for (Composition x: operands) {
//				x.topLevel(cx);
//			}
//		}
//
//		@Override public void asFilter(Context cx) {
//			for (Composition x: operands) {
//				x.asFilter(cx);
//			}
//		}
//
//		@Override public void tripleLevel(Context cx) {
//			cx.comment("generated from OR.tripleLevel", this);
//		}
//
//		@Override public void translatePureFilter(StringBuilder sb, Context cx) {
//			sb.append("(");
//			String or = "";
//			for (Composition x: operands) {
//				sb.append(or); or = " || ";
//				x.translatePureFilter(sb, cx);
//			}
//			sb.append(")");			
//		}
	}
	
	public static class Not extends Composition {
		
		public Not(List<Composition> operands) {
			super(COp.NOT, operands);
		}
//
//		@Override public void topLevel(Context cx) {
//			cx.comment("generated from (top-level)", this);
//		}
//
//		@Override public void asFilter(Context cx) {
//			cx.comment("generated from (asFilter)", this);
////			cx.generateFragment(" false ");
//		}
//
//		@Override public void tripleLevel(Context cx) {
//			cx.comment("generated from tripleLevel NOT", this);
//		}
//
//		@Override public void translatePureFilter(StringBuilder sb, Context cx) {
//			sb.append("false");
//		}
	}

	// TODO not
	public static Composition build(List<Filter> filters, List<SearchSpec> searchPatterns, Map<String, List<Composition>> compositions) {
		
//		System.err.println( ">> build: filters  " + filters );
//		System.err.println( ">> build: searches " + searchPatterns );			
		
		List<Composition> ands = compositions.get("@and");
		List<Composition> ors = compositions.get("@or");
		List<Composition> nots = compositions.get("@not");
		Composition fs = Composition.filters(filters, searchPatterns);
		
//		System.err.println( ">> ands: " + ands );
//		System.err.println( ">> ors:  " + ors );
//		System.err.println( ">> nots: " + nots );
//		System.err.println( ">> fs:   " + fs );		
	//
		List<Composition> expanded_ands = new ArrayList<Composition>(ands);
		if (nots.size() > 0) expanded_ands.add(negate(nots));
		if (filters.size() > 0 || searchPatterns.size() > 0) expanded_ands.add(fs);
		
//		System.err.println( ">> expanded_ands: " + expanded_ands );
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
			throw new UnsupportedOperationException("cannot negate AND");
		} else if (x instanceof Or) {
			throw new UnsupportedOperationException("cannot negate OR");			
		} else if (x instanceof Not) {
			throw new UnsupportedOperationException("cannot negate NOT");			
		} else if (x instanceof FilterWrap) {
			FilterWrap fs = (FilterWrap) x;
			return new FilterWrap(negate(fs.f));
		} 
		throw new BrokenException("Unhandled negate: " + x);
	}

	private static Filter negate(Filter f) {
		return new Filter(f.name, new Range(f.range.op.negate(), f.range.operands));
	}	

}
