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
			return a.getID().compareTo(b.getID());
		}
	};
	
	final List<Filter> filters;
	
	public DataQuery(List<Filter> filters) {
		this.filters = filters;
	}
	
	public List<Sort> sorts() {
		return new ArrayList<Sort>();
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
		catch (Exception e) { return null; }
	}

	private String toSparqlString(Problems p, Aspects a, PrefixMapping pm) {
		StringBuilder sb = new StringBuilder();
		Map<String, String> prefixes = pm.getNsPrefixMap();
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
		for (Filter f: filters) sf.put(f.name.asVar(), f);
	//
		sb.append( " SELECT ?item");
		for (Aspect x: ordered) {
			sb.append(" ").append( x.asVar() );
		}
	//	
		sb.append(" WHERE {");
		String dot = "";
		for (Aspect x: ordered) {  // for (Filter f: filters) {
			String fVar = x.asVar();
			sb.append(dot);
			sb.append(" ").append("?item").append(" ").append(x.asProperty()).append(" ").append(fVar);
			
			Filter f = sf.get(fVar);
			if (f != null) {
				String value = f.range.operands.get(0).asSparqlTerm();
				sb.append(" FILTER(" ).append(fVar).append( " = ").append(value).append(")");
			}
			
			
			dot = ". ";
		}
		sb.append( " }");
	//
		return sb.toString();
	}
}