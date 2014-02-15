/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ResultsToJson {
	
	public interface JSONConsumer {
		void consume(JSONWritable jv);
	}
	
	public static class Row implements JSONWritable {

		Map<String, Value> members = new HashMap<String, Value>();
		
		@Override public void writeTo(JSFullWriter jw) {
			jw.startObject();
			for (Map.Entry<String, Value> e: members.entrySet()) {
				Value v = e.getValue();
				if (v == null) {  } else { v.writeTo(jw); }
			}
			jw.finishObject();			
		}
		
		void put(String key, Value value) {
			members.put(key, value);
		}
		
		@Override public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Value> e: members.entrySet()) {
				sb.append( " " ).append(e.getKey());
			}
			return sb.toString();			
		}
		
	}
	
	public interface RowConsumer {
		public void consume(Row r);
	}
	
	public static void convert(Collection<Aspect> aspects, RowConsumer jc, Iterator<QuerySolution> rows) {
				
		Node current = null;
		Row pending = null;		
		
		Map<String, List<Value>> valuess = new HashMap<String, List<Value>>();
		Map<String, String> shorts = new HashMap<String, String>();
		
		for (Aspect a: aspects) 
			if (a.getIsMultiValued()) {
				valuess.put(a.asVar(), new ArrayList<Value>() );
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
					// loadFromValueLists(pending, valuess, shorts);
					jc.consume( pending );
				}
				pending = toRow(aspects, row); 
				pending.put("item", Value.fromNode("item", item) );
								
				current = item;
				
				for (Map.Entry<String, List<Value>> e: valuess.entrySet()) {
					e.getValue().clear();
					String key = e.getKey();
					e.getValue().add( Value.fromNode(key, row.get(key).asNode()));
				}
			}
		}
		
		if (pending != null) {
			// loadFromValueLists(pending, valuess, shorts);
			jc.consume(pending);
		}
	}
	
	public static Row toRow(Collection<Aspect> aspects, QuerySolution qs) {
		
//		System.err.println( ">> qs: " );
//		Iterator<String> it = qs.varNames();
//		while (it.hasNext()) System.err.print( " " + it.next() );
//		System.err.println();
		
		Row result = new Row();
		for (Aspect a: aspects) {
			String key = a.getName().getCURIE();		
			RDFNode value = qs.get(a.asVar());
		//			
			if (value == null) {
				result.put(key, null);				
			}
			else {
				Value v = Value.fromNode(key, value.asNode());
				boolean mv = a.getIsMultiValued();
				if (mv) System.err.println( ">> got multivalued " + v);
				result.put(key, (mv ? v : v));
			}
		}
		return result;
	}

	// load the appropriate fields of pending from the value sets accumulated
	// in valuess.
	private static void loadFromValueLists(JsonObject pending,	Map<String, List<JsonValue>> valuess, Map<String, String> shorts) {
		for (Map.Entry<String, List<JsonValue>> e: valuess.entrySet()) {	
			String curi = shorts.get(e.getKey());
			pending.put(curi, jsonArrayNoDuplicatesFrom(e.getValue()));
		}
	}

	// load the appropriate values sets with the JSON values converted from
	// result-set format.
	private static void multipleValueFetch(Map<String, List<Value>> valuess, QuerySolution row) {
		for (Map.Entry<String, List<Value>> e: valuess.entrySet()) {
			e.getValue().add( Value.fromNode(e.getKey(), row.get(e.getKey()).asNode()));
		}
	}

	private static JsonValue jsonArrayNoDuplicatesFrom(Collection<JsonValue> values) {
		JsonArray result = new JsonArray();
		for (JsonValue v: values)
			if (!result.contains(v)) result.add( v );
		return result;
	}

	private static JsonValue jsonArrayFrom(Collection<JsonValue> values) {
		JsonArray result = new JsonArray();
		for (JsonValue v: values) result.add( v );
		return result;
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
