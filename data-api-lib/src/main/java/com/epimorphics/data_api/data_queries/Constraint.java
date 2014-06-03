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
import com.epimorphics.data_api.sparql.SQ_Const;
import com.epimorphics.data_api.sparql.SQ_Variable;

public abstract class Constraint {
		
	public abstract Constraint negate();
	public abstract void tripleFiltering(Context cx);
	
	public static final Constraint EMPTY = new True();
	
	static final String nl = "\n";

	/** 
	 	Translate this constraint at the top level of SPARQL. Many constraints 
	 	can be handled by this default method because they're just ANDed 
	 	sub-constraints.
	 * @param p TODO
	*/
	
	public void translate(Problems p, Context cx) {
		
		boolean fullyGeneral = true;
		
		if (fullyGeneral && (cx.dq.slice.length != null || cx.dq.slice.offset != null)) {
			
			
			
		}
		
		List<Guard> guards = cx.dq.guards;
		boolean needsDistinct = false;
		boolean baseQueryNeeded = true;  
		
		for (Guard guard : guards) {
			if (guard.needsDistinct()) needsDistinct = true;
			if (guard.supplantsBaseQuery()) baseQueryNeeded = false;
		}
		
		cx.sq.addSelectedVar(SQ_Const.item, needsDistinct);
		
		for (Aspect a: cx.ordered) {
			cx.sq.addSelectedVar(new SQ_Variable(a.asVarName()));
		}
        
		String baseQuery = cx.api.getBaseQuery();
		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded)
			cx.sq.addBaseQuery(baseQuery);
	//
		
		int ng = guards.size();
        // cx.comment(ng == 0 ? "no guards" : ng == 1 ? "one guard" : ng + " guards");
        for (Guard g: guards) cx.sq.addQueryFragment(g.queryFragment(cx.api));
        
		Constraint unEquals = cx.declareAspectVarsSQ(cx.earlySearchesSQ(this));
		unEquals.tripleFiltering(cx);
		
		if (cx.dq.slice.length != null) cx.sq.setLimit(cx.dq.slice.length);
		if (cx.dq.slice.offset != null) cx.sq.setOffset(cx.dq.slice.offset);
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
