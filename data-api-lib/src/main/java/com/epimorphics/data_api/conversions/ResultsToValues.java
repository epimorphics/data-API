/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.*;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ResultsToValues {
	
	public interface RowConsumer {
		public void consume(Row r);
	}
	
	public static void convert(Collection<Aspect> aspects, RowConsumer jc, Iterator<QuerySolution> rows) {
				
		Node current = null;
		Row pending = null;		
		
		Map<String, List<ResultValue>> valuess = new HashMap<String, List<ResultValue>>();
		Map<String, String> shorts = new HashMap<String, String>();
		
		for (Aspect a: aspects) 
			if (a.getIsMultiValued()) {
				valuess.put(a.asVar(), new ArrayList<ResultValue>() );
				shorts.put(a.asVar(), a.getName().getCURIE() );
			}
		
		while (rows.hasNext()) {

			QuerySolution row = rows.next();
			Node item = row.get("item").asNode();
			
			if (item.equals(current)) {
				multipleValueFetch(valuess, row);
			} else {
				// new item, flush any existing item & reset current
				if (pending != null) {
					loadFromValueLists(pending, valuess, shorts);
					jc.consume( pending );
				}
				pending = toRow(aspects, row); 
				pending.put("item", ResultValue.fromNode(item) );
								
				current = item;
				
				for (Map.Entry<String, List<ResultValue>> e: valuess.entrySet()) {
					e.getValue().clear();
					String key = e.getKey();
					e.getValue().add( ResultValue.fromNode(row.get(key).asNode()));
				}
			}
		}
		
		if (pending != null) {
			loadFromValueLists(pending, valuess, shorts);
			jc.consume(pending);
		}
	}
	
	public static Row toRow(Collection<Aspect> aspects, QuerySolution qs) {
		
		Row result = new Row();
		for (Aspect a: aspects) {
			String key = a.getName().getCURIE();		
			RDFNode value = qs.get(a.asVar());
		//			
			if (value == null) {
				result.put(key, null);				
			}
			else {
				ResultValue v = ResultValue.fromNode(value.asNode());
				// boolean mv = a.getIsMultiValued();
				// if (mv) System.err.println( ">> got multivalued " + v);
				result.put(key, v); // (mv ? v : v));
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
			e.getValue().add( ResultValue.fromNode(row.get(e.getKey()).asNode()));
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
