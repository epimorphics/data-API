/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.*;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ResultsToRows {

	private final Compactions c;
	private final Collection<Aspect> aspects;
	
	public ResultsToRows(Collection<Aspect> aspects, Compactions c) {
		this.c = c;
		this.aspects = aspects;
	}

	public void convert(RowConsumer rc, Iterator<QuerySolution> solutions) {
						
		Node current = null;
		Row pending = null;		
		
		Map<String, List<Term>> valuess = new HashMap<String, List<Term>>();
		Map<String, String> shorts = new HashMap<String, String>();
		
		for (Aspect a: aspects) 
			if (a.getIsMultiValued()) {
				valuess.put(a.asVarName(), new ArrayList<Term>() );
				shorts.put(a.asVarName(), a.getName().getCURIE() );
			}
		
		while (solutions.hasNext()) {
			
			QuerySolution sol = solutions.next();
			Node item = sol.get("item").asNode();
			
			if (item.equals(current)) {
				multipleValueFetch(valuess, sol);
			} else {
				// new item, flush any existing item & reset current
				if (pending != null) {
					loadFromValueLists(pending, valuess, shorts);
					rc.consume( pending );
				}
				pending = solutionToRow(sol); 
				pending.put("@id", Term.string(item.getURI() ) );
								
				current = item;
				
				for (Map.Entry<String, List<Term>> e: valuess.entrySet()) {
					e.getValue().clear();
					String key = e.getKey();
					RDFNode r = sol.get(key);
					if (r != null) e.getValue().add( Term.fromNode(c, r.asNode()));
				}
			}
		}
		
		if (pending != null) {
			loadFromValueLists(pending, valuess, shorts);
			rc.consume(pending);
		}
	}
	
	static final Term none = Term.array(new ArrayList<Term>());
	
	public Row solutionToRow(QuerySolution qs) {
		Row result = new Row();
		for (Aspect a: aspects) {
			String key = a.getName().getCURIE();		
			RDFNode value = qs.get(a.asVarName());
		//			
			if (value == null) {
				result.put(key, none);				
			} else {
				Term v = Term.fromNode(c, value.asNode());
				boolean ov = a.getIsOptional();
				if (ov) {
					result.put(key, Term.array(BunchLib.list(v)));
				} else {
					result.put(key, v);					
				}
			}
		}
		return result;
	}

	// load the appropriate fields of pending from the value sets accumulated
	// in valuess.
	private void loadFromValueLists(Row pending, Map<String, List<Term>> valuess, Map<String, String> shorts) {
		for (Map.Entry<String, List<Term>> e: valuess.entrySet()) {	
			String curi = shorts.get(e.getKey());
			pending.put(curi, valueArrayNoDuplicatesFrom(e.getValue()));
		}
	//
		if (c.squeezeValues()) {
			for (Aspect a: aspects) {
				if (a.getIsMultiValued() == false && a.getIsOptional()) {
					pending.squeezeOptionalProperty(a.getName().getCURIE());
				}
			}
		}
	}

	// load the appropriate values sets with the JSON values converted from
	// result-set format.
	private void multipleValueFetch(Map<String, List<Term>> valuess, QuerySolution row) {
		for (Map.Entry<String, List<Term>> e: valuess.entrySet()) {
			RDFNode r = row.get(e.getKey());
			if (r != null) e.getValue().add( Term.fromNode(c, r.asNode()));
		}
	}

	private static Term valueArrayNoDuplicatesFrom(Collection<Term> values) {
		List<Term> result = new ArrayList<Term>();
		for (Term v: values)
			if (!result.contains(v)) result.add( v );
		return Term.array(result);
	}

	public List<Row> convert(List<QuerySolution> rows) {
		final List<Row> result = new ArrayList<Row>();
		RowConsumer consumeToArray = new RowConsumer() {
			@Override public void consume(Row jo) { result.add( jo ); }
		};
		convert(consumeToArray, rows.iterator() );
		return result;
	}
}
