/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Composition {
	
	public static final Composition NONE = new Composition
		( "none"
		, new ArrayList<Composition>() 
		);
	
	final String op;
	final List<Composition> operands;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(op);
		for (Composition c: operands) sb.append(" ").append(c.toString());
		sb.append(")");
		return sb.toString();
	}
	
	public Composition(String op, List<Composition> operands) {
		this.op = op;
		this.operands = operands;
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
	
	public static Composition filters(List<Filter> filters ) {
		return new Filters(filters);
	}
	
	public static class Filters extends Composition {
		
		final List<Filter> filters;
		
		public Filters(List<Filter> filters) {
			super("filter", new ArrayList<Composition>() );
			this.filters = filters;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("(").append(op);
			sb.append(" ").append(filters);
			sb.append(")");
			return sb.toString();
		}
	}
	
	public static class And extends Composition {

		public And(List<Composition> operands) {
			super("and", operands);
		}
	}
	
	public static class Or extends Composition {
		
		public Or(List<Composition> operands) {
			super("or", operands);
		}
	}
	
	public static class Not extends Composition {
		
		public Not(List<Composition> operands) {
			super("not", operands);
		}
	}

	// TODO not
	public static Composition build(List<Filter> filters, Map<String, List<Composition>> compositions) {
		
		System.err.println( ">> build: filters " + filters );		
		
		List<Composition> ands = compositions.get("@and");
		List<Composition> ors = compositions.get("@or");
		// List<Composition> nots = compositions.get("@not");
		Composition fs = Composition.filters(filters);
	//
		List<Composition> expanded_ands = new ArrayList<Composition>(ands);
		if (filters.size() > 0) expanded_ands.add(fs);
	//
		List<Composition> expanded_ors = new ArrayList<Composition>(ors);
		if (expanded_ands.size() > 0) expanded_ors.add(Composition.and(expanded_ands));
		Composition result = Composition.or(expanded_ors);
		
		if (result.operands.size() == 0 && result.op.equals("or")) result = NONE;
		System.err.println( ">> built: " + result );
		
		return result;		
	}

	public static String allToSparql(Composition c) {
		StringBuilder sb = new StringBuilder();	
		sb.append("\n\n");
		
		sb.append("COMPOSITION\n    ").append(c).append("\n");
		allToSparql(sb, c);
		return sb.toString();
	}
	
	public static void allToSparql(StringBuilder sb, Composition c) {
		sb.append("GLOBAL STUFF\n");
		toSparql(sb, c);
	}
	
	public static void toSparql(StringBuilder sb, Composition c) {
		if (c instanceof And) {
			for (Composition x: c.operands) toSparql(sb, x);
		} else if (c instanceof Or) {
			sb.append("NESTED SELECT for " + c + "\n");
			sb.append("  {\n");
			String u = "";
			for (Composition x: c.operands) {
				sb.append(u); u = " UNION ";
				sb.append("{"); toSparql(sb, x); sb.append("}\n" );
			}
			sb.append("  }\n");
			sb.append("END NESTED SELECT\n" );
		} else if (c instanceof Filters) {
			Filters f = (Filters) c;
			sb.append("FILTERS ").append(f.filters).append("\n");
		} else {
			throw new RuntimeException("NOT IMPLEMENTED: " + c );
		}
			
	}
	

}
