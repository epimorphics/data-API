/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.*;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.libs.BunchLib;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ResultsToValues {
	
	public static void convert(Collection<Aspect> aspects, RowConsumer rc, Iterator<QuerySolution> solutions) {
				
		Node current = null;
		Row pending = null;		
		
		Map<String, List<ResultValue>> valuess = new HashMap<String, List<ResultValue>>();
		Map<String, String> shorts = new HashMap<String, String>();
		
		for (Aspect a: aspects) 
			if (a.getIsMultiValued()) {
				valuess.put(a.asVar(), new ArrayList<ResultValue>() );
				shorts.put(a.asVar(), a.getName().getCURIE() );
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
				pending = solutionToRow(aspects, sol); 
				pending.put("item", ResultValue.fromNode(item) );
								
				current = item;
				
				for (Map.Entry<String, List<ResultValue>> e: valuess.entrySet()) {
					e.getValue().clear();
					String key = e.getKey();
					RDFNode r = sol.get(key);
					if (r != null) e.getValue().add( ResultValue.fromNode(r.asNode()));
				}
			}
		}
		
		if (pending != null) {
			loadFromValueLists(pending, valuess, shorts);
			rc.consume(pending);
		}
	}
	
	static final ResultValue none = ResultValue.array(new ArrayList<ResultValue>());
	
	public static Row solutionToRow(Collection<Aspect> aspects, QuerySolution qs) {
		Row result = new Row();
		for (Aspect a: aspects) {
			String key = a.getName().getCURIE();		
			RDFNode value = qs.get(a.asVar());
		//			
			if (value == null) {
				result.put(key, none);				
			}
			else {
				ResultValue v = ResultValue.fromNode(value.asNode());
				boolean ov = a.getIsOptional();
				if (ov) {
					result.put(key, ResultValue.array(BunchLib.list(v)));
				} else {
					result.put(key, v);					
				}
			}
		}
		return result;
	}

	// load the appropriate fields of pending from the value sets accumulated
	// in valuess.
	private static void loadFromValueLists(Row pending,	Map<String, List<ResultValue>> valuess, Map<String, String> shorts) {
		for (Map.Entry<String, List<ResultValue>> e: valuess.entrySet()) {	
			String curi = shorts.get(e.getKey());
			pending.put(curi, valueArrayNoDuplicatesFrom(e.getValue()));
		}
	}

	// load the appropriate values sets with the JSON values converted from
	// result-set format.
	private static void multipleValueFetch(Map<String, List<ResultValue>> valuess, QuerySolution row) {
		for (Map.Entry<String, List<ResultValue>> e: valuess.entrySet()) {
			RDFNode r = row.get(e.getKey());
			if (r != null) e.getValue().add( ResultValue.fromNode(r.asNode()));
		}
	}

	private static ResultValue valueArrayNoDuplicatesFrom(Collection<ResultValue> values) {
		List<ResultValue> result = new ArrayList<ResultValue>();
		for (ResultValue v: values)
			if (!result.contains(v)) result.add( v );
		return ResultValue.array(result);
	}

	public static List<Row> convert(List<Aspect> aspects, List<QuerySolution> rows) {
		final List<Row> result = new ArrayList<Row>();
		RowConsumer consumeToArray = new RowConsumer() {
			@Override public void consume(Row jo) { result.add( jo ); }
		};
		convert(aspects, consumeToArray, rows.iterator() );
		return result;
	}
}
