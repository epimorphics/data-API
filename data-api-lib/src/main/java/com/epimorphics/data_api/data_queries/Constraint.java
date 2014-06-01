/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Context.Equalities;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ.Variable;

public abstract class Constraint {

	public abstract void toSparql(Context cx, String varSuffix);
	
	public abstract void toFilterBody(Context cx, String varSuffix);
	
	public abstract Constraint negate();

	public abstract String toString();
	
	// NEW BITS /////////////////////////////////////////////////////
	
	static final String nl = "\n";

	/** 
	 	Translate this constraint at the top level of SPARQL. Many constraints 
	 	can be handled by this default method because they're just ANDed 
	 	sub-constraints.
	 * @param p TODO
	*/
	
	public void translate(Problems p, Context cx) {
		cx.sq.addOutput(new SQ.Variable("item"));
		
		for (Aspect a: cx.ordered) {
			cx.sq.addOutput(new SQ.Variable(a.asVarName()));
		}
		
		List<Guard> guards = cx.dq.guards;
		boolean needsDistinct = false;
		boolean baseQueryNeeded = true;  
		
		for (Guard guard : guards) {
			if (guard.needsDistinct()) needsDistinct = true;
			if (guard.supplantsBaseQuery()) baseQueryNeeded = false;
		}
		
		if (cx.dq.isCountQuery()) {
			System.err.println(">> IGNORING count queries for now.");
		}
        
		String baseQuery = cx.api.getBaseQuery();
		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded)
			cx.sq.addBaseQuery(baseQuery);
	//
		
		int ng = guards.size();
        cx.comment(ng == 0 ? "no guards" : ng == 1 ? "one guard" : ng + " guards");
        if (ng > 0) System.err.println( ">> IGNORING guards for now.");
//        for (Guard guard : guards)
//        	cx.out.append(guard.queryFragment(cx.api));
    // 
		
		Constraint unEquals = cx.declareAspectVars(cx.earlySearches(this));
		
		unEquals.tripleFiltering(cx);
		cx.out.append("}");
		cx.dq.querySort(cx.out);
		
		if (cx.dq.slice.length != null) cx.sq.setLimit(cx.dq.slice.length);
		if (cx.dq.slice.offset != null) cx.sq.setOffset(cx.dq.slice.offset);

		// if (cx.dq.isNestedCountQuery()) cx.out.append("}");
	}
	
	public void topLevelSparql(Problems p, Context cx) {    	

		List<Guard> guards = cx.dq.guards;
		boolean needsDistinct = false;
		for (Guard guard : guards) if (guard.needsDistinct()) needsDistinct = true;
		
		if (cx.dq.isCountQuery()) {
			cx.out.append("SELECT")
				.append(" (COUNT(")
				.append(needsDistinct ? " DISTINCT" : "")
				.append("?item")
				.append(") AS ?_count)")
				.append(nl)
				;
			if (cx.dq.isNestedCountQuery()) {
            	cx.comment("this is a nested count query, so:");
            	cx.out.append("  { SELECT ?item").append("\n");
            }
		} else {
			cx.out.append("SELECT")
			.append(needsDistinct ? " DISTINCT" : "")
			.append("?item")
			.append(nl)
			;
			
			for (Aspect a: cx.ordered) {
				cx.out.append("  ").append(a.asVar()).append(nl);
			}			
		}
		cx.out.append("WHERE {").append(nl);        

        boolean baseQueryNeeded = true;  
        
        for (Guard guard : guards) {
            if (guard.supplantsBaseQuery()) {
                baseQueryNeeded = false;
            }
        }
        
		String baseQuery = cx.api.getBaseQuery();
		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded) {
			cx.comment("base query");
		    cx.out.append( "  ").append(baseQuery).append( "\n");
		} else {
			cx.comment("no base query");
		}
	//
		int ng = guards.size();
        cx.comment(ng == 0 ? "no guards" : ng == 1 ? "one guard" : ng + " guards");
        for (Guard guard : guards)
        	cx.out.append(guard.queryFragment(cx.api));
    // 
		
		Constraint unEquals = cx.declareAspectVars(cx.earlySearches(this));
		
		unEquals.tripleFiltering(cx);
		cx.out.append("}");
		cx.dq.querySort(cx.out);
		if (cx.dq.slice.length != null) cx.out.append( " LIMIT " ).append(cx.dq.slice.length);
		if (cx.dq.slice.offset != null) cx.out.append( " OFFSET " ).append(cx.dq.slice.offset);

		if (cx.dq.isNestedCountQuery()) cx.out.append("}");
		}

	public void tripleFiltering(Context cx) {
		throw new UnsupportedOperationException("cannot triple-filter this " + getClass().getSimpleName() + ": " + this);
	}
	
	// END OF NEW BITS //////////////////////////////////////////////
	
	@Override public boolean equals(Object other) {
		return this.getClass() == other.getClass() && same((Constraint) other);
	}
	
	protected abstract boolean same(Constraint other);

	public Constraint() {
	}
	
	public static final Constraint EMPTY = new True();
	
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
