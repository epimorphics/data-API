/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

public class DataQuery {
	
	private static final Comparator<? super Aspect> compareAspects = new Comparator<Aspect>() {

		@Override public int compare(Aspect a, Aspect b) {
			if (a.getIsOptional() == b.getIsOptional()) return a.getID().compareTo(b.getID());
			return a.getIsOptional() ? +1 : -1;
		}
	};
	
	final List<Filter> filters;
	final List<Sort> sortby;

	public DataQuery(List<Filter> filters, List<Sort> sortby) {
		this.filters = filters;
		this.sortby = sortby;
	}
	
	public DataQuery(List<Filter> filters) {
		this(filters, new ArrayList<Sort>() );
	}
	
	public List<Sort> sorts() {
		return sortby;
	}
	
	public List<Filter> filters() {
		return filters;
	}
	
	public String lang() {
		return null;
	}
	
	public Slice slice() {
		return new Slice();
	}
	
	public String toSparql(Problems p, Aspects a, PrefixMapping pm) {
		try { return toSparqlString(p, a, pm); }
		catch (Exception e) { p.add("exception generating SPARQL query: " + e.getMessage()); e.printStackTrace(System.err); return null; }
	}

	private String toSparqlString(Problems p, Aspects a, PrefixMapping pm) {
		StringBuilder sb = new StringBuilder();
		Map<String, String> prefixes = pm.getNsPrefixMap();
	//
		boolean needsSKOS = false;
		
		for (Filter f: filters) {
			if (f.range.op.equals("below")) needsSKOS = true;
		}
		if (needsSKOS) prefixes.put("skos", "http://www.w3.org/2004/02/skos/core");
	//
		for (String key: prefixes.keySet()) 
			sb.append( "PREFIX " )
			.append( key ).append(": " )
			.append( "<" ).append( prefixes.get(key)).append(">")
			.append( "\n" )
			;
	//
		List<Aspect> ordered = new ArrayList<Aspect>(a.getAspects());
		Collections.sort(ordered, compareAspects);
	//
		Map<String, Filter> sf = new HashMap<String, Filter>();
		for (Filter f: filters) sf.put("?" + f.name.asVar(), f);
	//
		sb.append( " SELECT ?item");
		for (Aspect x: ordered) {
			sb.append(" ?").append( x.asVar() );
		}
	//	
		sb.append(" WHERE {");
		String dot = "";
		for (Aspect x: ordered) {
			String fVar = "?" + x.asVar();
			sb.append(dot);
			if (x.getIsOptional()) sb.append( " OPTIONAL {" );
			sb.append(" ").append("?item").append(" ").append(x.asProperty()).append(" ").append(fVar);
			if (x.getIsOptional()) sb.append( " }" );
		//
			Filter f = sf.get(fVar);
			if (f != null) {		
				String value = f.range.operands.get(0).asSparqlTerm();
				String rangeOp = f.getRangeOp();	
				if (rangeOp.equals("oneof")) {
					String orOp = "";
					List<Term> operands = f.range.operands;
					sb.append(" FILTER(" );
					for (Term v: operands) {
						sb.append(orOp).append(fVar).append( " = ").append(v.asSparqlTerm());
						orOp = " || ";
					}
					sb.append(")");
				} else if (rangeOp.equals("below")) {
					Shortname below = x.getBelowPredicate();
					sb.append(". ").append(fVar).append(" ").append(below.getCURIE()).append(" ").append(value);
				} else if (rangeOp.equals("contains")) {
					sb.append(". ").append("FILTER(").append("CONTAINS(").append(fVar).append(", ").append(value).append(")").append(")");
				} else if (rangeOp.equals("matches")) {
					sb.append(". ").append("FILTER(").append("REGEX(").append(fVar).append(", ").append(value).append(")").append(")");
				} else if (rangeOp.equals("search")) {
					sb.append(". ").append(fVar).append(" <http://jena.apache.org/text#query> ").append(value);
				} else {
					String op = opForFilter(f);
					sb.append(" FILTER(" ).append(fVar).append(" ").append(op).append(" ").append(value).append(")");
				}
			}
			dot = ". ";
		}
		sb.append( " }");
	//
		if (sortby.size() > 0) {
			sb.append(" ORDER BY");
			for (Sort s: sortby) {
				sb.append(" ");
				if (!s.upwards) sb.append("DESC(");
				sb.append( "?" ).append(s.by.asVar());
				if (!s.upwards)sb.append(")"); 
			}
			
		}
	//
		return sb.toString();
	}

	private String opForFilter(Filter f) {
		String rangeOp = f.getRangeOp();
		return opFor(rangeOp);
	}
	
	private String opFor(String rangeOp) {
		if (rangeOp.equals("eq")) return "=";
		if (rangeOp.equals("ne")) return "!=";
		if (rangeOp.equals("le")) return "<=";
		if (rangeOp.equals("lt")) return "<";
		if (rangeOp.equals("ge")) return ">=";
		if (rangeOp.equals("gt")) return ">";
		throw new RuntimeException("should never happen: unexpected range op '" + rangeOp + "'");
	}
}