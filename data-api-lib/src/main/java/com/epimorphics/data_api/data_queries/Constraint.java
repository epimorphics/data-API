/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ_Const;
import com.epimorphics.data_api.sparql.SQ_Variable;

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

	/** 
	 	Translate this constraint at the top level of SPARQL. 
	*/
	public void translate(Problems p, Context cx) {
		
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
			addLengthAndOffset(nested, length, offset);
			cx.sq.comment("fully general case because constraints with multi-valued aspects.");
			cx.sq.addSubquery(nested);
						
		} else {
			baseQueryAndGuards(cx, baseQuery, guards, baseQueryNeeded, cx.sq);
			Constraint unEquals = cx.declareAspectVarsSQ(cx.earlySearchesSQ(this));
			unEquals.tripleFiltering(cx);
			addLengthAndOffset(cx.sq, length, offset);
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
