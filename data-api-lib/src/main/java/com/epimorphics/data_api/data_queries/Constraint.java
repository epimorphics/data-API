/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ_Const;
import com.epimorphics.data_api.sparql.SQ_Expr;
import com.epimorphics.data_api.sparql.SQ_Filter;
import com.epimorphics.data_api.sparql.SQ_Node;
import com.epimorphics.data_api.sparql.SQ_Resource;
import com.epimorphics.data_api.sparql.SQ_TermAsNode;
import com.epimorphics.data_api.sparql.SQ_Triple;
import com.epimorphics.data_api.sparql.SQ_Variable;
import com.hp.hpl.jena.shared.PrefixMapping;

public abstract class Constraint {
		
	public abstract Constraint negate();
	
	public abstract void tripleFiltering(Context cx);
	
	public static final Constraint EMPTY = new True();
	
	public static final String nl = "\n";
	
	/**
	     Returns true iff this constraint binds aspect a, ie if there's a
	     filter in the constraint tree that mentions it.
	*/
	protected abstract boolean constrains(Aspect a);
	
	abstract void doAspect(State s, Aspect a);
	
	/** 
	 	Translate this constraint at the top level of SPARQL. 
	*/
	public void translate(Problems p, Context cx) {
		
		alternativeTranslation(p, cx);
		
		if (true) return;
		
		String baseQuery = cx.api.getBaseQuery();
		List<Guard> guards = cx.dq.guards;
		boolean needsDistinct = false;
		boolean baseQueryNeeded = true;  
		Integer length = cx.dq.slice.length,  offset = cx.dq.slice.offset;
		
		for (Guard guard : guards) {
			if (guard.needsDistinct()) needsDistinct = true;
			if (guard.supplantsBaseQuery()) baseQueryNeeded = false;
		}

		cx.sq.comment(cx.ordered.size() + " aspect variables");	
		cx.sq.addSelectedVar(SQ_Const.item, needsDistinct);

		for (Aspect a: cx.ordered) {
			cx.sq.addSelectedVar(new SQ_Variable(a.asVarName()));
		}

		boolean fullyGeneral = constrainsMultivaluedAspect(cx.ordered);
		
		if (fullyGeneral && (length != null || offset != null)) {
			SQ nested = new SQ();
			Context rx = new Context( nested, cx.out, cx.dq, p, cx.api );
			
			nested.addSelectedVar(SQ_Const.item, true);
			
			baseQueryAndGuards(cx, baseQuery, guards, baseQueryNeeded, nested);
	        
	        cx.declareAspectVarsSQ(EMPTY);
			Constraint unEquals = rx.declareAspectVarsSQ(cx.earlySearchesSQ(this));
			unEquals.tripleFiltering(rx);
			cx.sq.comment("variables declared, filters follow.");
			addLengthAndOffset(nested, length, offset);
			cx.sq.comment("fully general case because constraints with multi-valued aspects.");
			cx.sq.addSubquery(nested);
						
		} else {
			baseQueryAndGuards(cx, baseQuery, guards, baseQueryNeeded, cx.sq);
			Constraint unEquals = cx.declareAspectVarsSQ(cx.earlySearchesSQ(this));
			cx.sq.comment("variables declared, filters follow.");
			unEquals.tripleFiltering(cx);
			addLengthAndOffset(cx.sq, length, offset);
		}
	}
	
	private void alternativeTranslation(Problems p, Context cx) {
		
		String baseQuery = cx.api.getBaseQuery();
		List<Guard> guards = cx.dq.guards;
		boolean needsDistinct = false;
		boolean baseQueryNeeded = true;  
		Integer length = cx.dq.slice.length,  offset = cx.dq.slice.offset;
		
		for (Guard guard : guards) {
			if (guard.needsDistinct()) needsDistinct = true;
			if (guard.supplantsBaseQuery()) baseQueryNeeded = false;
		}

		cx.sq.comment(cx.ordered.size() + " aspect variables");	
		cx.sq.addSelectedVar(SQ_Const.item, needsDistinct);

		for (Aspect a: cx.ordered) {
			cx.sq.addSelectedVar(new SQ_Variable(a.asVarName()));
		}

		baseQueryAndGuards(cx, baseQuery, guards, baseQueryNeeded, cx.sq);
		
//		Constraint unEquals = cx.declareAspectVarsSQ(cx.earlySearchesSQ(this));
//		cx.sq.comment("variables declared, filters follow.");
//		unEquals.tripleFiltering(cx);
		
		for (Aspect a: cx.ordered) {
			if (!a.getIsOptional()) {
				State s = new State(cx.api.getPrefixes(), cx);
				
//				System.err.println(">> mandatory aspect " + a);
				
				for (Constraint x: operands()) {
//					System.err.println(">> .. constraint " + x + " " + x.getClass().getSimpleName());
					if (x.constrains(a)) {
//						System.err.println(">> .... doing " + x);
						x.doAspect(s, a);
					}
				}
				
//				System.err.println(">> done " + a);
				
				s.done(a);
			}
		}
		
		for (Aspect a: cx.ordered) {
			if (a.getIsOptional()) {
				State s = new State(cx.api.getPrefixes(), cx);
				for (Constraint x: operands()) {
					if (x.constrains(a)) {
						x.doAspect(s, a);
					}
				}				
				s.done(a);
			}			
		}
		
		addLengthAndOffset(cx.sq, length, offset);	
	}
	
	static class State {
		
		final PrefixMapping pm;
		final Context cx;
		
		final SQ sq;
		
		boolean defined = false;
		
		State(PrefixMapping pm, Context cx) {
			this.pm = pm;
			this.cx = cx;
			this.sq = cx.sq;
		}

		void done(Aspect a) {
			if (!defined) {
				SQ_Node theProperty = new SQ_Resource(a.asProperty());
				SQ_Variable theVariable = new SQ_Variable(a.asVarName());
				SQ_Triple x = new SQ_Triple(SQ_Const.item, theProperty, theVariable);
				if (a.getIsOptional()) sq.addOptionalTriple(x);
				else sq.addTriple(x);
			}
		}
		
		public void hasObject(Aspect a, Term t) {
			System.err.println("!! have to deal with property paths for " + a);
			SQ_Node theProperty = new SQ_Resource(a.asProperty());
			SQ_TermAsNode value = new SQ_TermAsNode(pm, t);
			SQ_Triple x = new SQ_Triple(SQ_Const.item, theProperty, value);
			SQ_Variable var = new SQ_Variable(a.asVarName());
			sq.addTriple(x);
			defined = true;
			sq.addBind(value, var);
		}

		public void filter(Aspect a, Operator op, Term t) {
			List<SQ_Expr> operands = new ArrayList<SQ_Expr>();
			operands.add(new SQ_TermAsNode(pm, t));
			SQ_Filter f = new SQ_Filter(op, a, operands);
			sq.addFilter(f);
		}
		
	}
	
	protected List<Constraint> operands() {
		List<Constraint> result = new ArrayList<Constraint>();
		operands(result);
		return result;
	}
	
	private void operands(List<Constraint> result) {
		if (this instanceof And) {
			for (Constraint c: ((And) this).operands) {
				result.add(c);
			}
		} else {
			result.add(this);
		}
	}

	private void baseQueryAndGuards(Context cx, String baseQuery, List<Guard> guards, boolean baseQueryNeeded, SQ sq) {
		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded) {
			sq.comment("base query");
			sq.addBaseQuery(baseQuery);
		} else {
			sq.comment("no base query");
		}
	//
		int ng = guards.size();
		sq.comment(ng == 0 ? "no guards" : ng == 1 ? "one guard" : ng + " guards");
		for (Guard g: guards) sq.addQueryFragment(g.queryFragment(cx.api));
	}
	
	private void addLengthAndOffset(SQ sq, Integer length, Integer offset) {
		if (length != null) sq.setLimit(length);
		if (offset != null) sq.setOffset(offset);
	}
	
	/**
	    Returns true iff there is a bound multi-valued aspect in the
	    collection.
	*/
	private boolean constrainsMultivaluedAspect(List<Aspect> ordered) {
		for (Aspect a: ordered)
			if (a.getIsMultiValued())
				if (constrains(a)) return true;
		return false;
	}
	
	@Override public boolean equals(Object other) {
		return this.getClass() == other.getClass() && same((Constraint) other);
	}
	
	protected abstract boolean same(Constraint other);

	public Constraint() {
	}
	
	public static Constraint and(List<Constraint> operands) {
		if (operands.size() == 0) return EMPTY;
		if (operands.size() == 1) return operands.get(0);
		return new And(operands);
	}
	
	public static Constraint or(List<Constraint> operands) {
		if (operands.size() == 0) return EMPTY;
		if (operands.size() == 1) return operands.get(0);
		return new Or(operands);
	}
	
	public static Constraint filters(List<Constraint> filters) {
		return filters(filters, SearchSpec.none());
	}
	
	public static Constraint filters(List<Constraint> filters, List<SearchSpec> searchPatterns ) {
		List<Constraint> operands = new ArrayList<Constraint>(filters.size());
		operands.addAll(filters);
		operands.addAll(searchPatterns);
		return and(operands);
	}

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
		for (Constraint n: nots) x.add(n.negate());
		return x.size() == 1 ? x.get(0) : and(x);
	}	

}
