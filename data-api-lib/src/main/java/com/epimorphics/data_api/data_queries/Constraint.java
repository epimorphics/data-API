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
	
	public static Constraint filters(List<Constraint> filters) {
		return filters(filters, SearchSpec.none());
	}
	
	public static Constraint filters(List<Constraint> filters, List<SearchSpec> searchPatterns ) {
		List<Constraint> operands = new ArrayList<Constraint>(filters.size());
		operands.addAll(filters);
		operands.addAll(searchPatterns);
		return and(operands);
	}

	public static Constraint smallOr( Constraint A, Constraint B ) {
		return new FilterOr(A, B);		
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
		} else if (c instanceof Filter) {
			Filter f = (Filter) c;
			return negate(f);
		} 
		throw new BrokenException("Unhandled negate: " + c);
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
