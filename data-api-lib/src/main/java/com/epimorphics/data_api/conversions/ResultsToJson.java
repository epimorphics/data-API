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
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;

public class ResultsToJson {
	
	public interface JSONConsumer {
		void consume(JsonValue jv);
	}
	
	public static void convert(Collection<Aspect> aspects, JSONConsumer jc, Iterator<QuerySolution> rows) {
				
		Node current = null;
		JsonObject pending = null;		
		
		Map<String, List<JsonValue>> valuess = new HashMap<String, List<JsonValue>>();
		Map<String, String> shorts = new HashMap<String, String>();
		
		for (Aspect a: aspects) 
			if (a.getIsMultiValued()) {
				valuess.put(a.asVar(), new ArrayList<JsonValue>() );
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
				pending = Convert.toJson(aspects, row); 
				pending.put("item", Convert.toJson(item) );
								
				current = item;
				
				for (Map.Entry<String, List<JsonValue>> e: valuess.entrySet()) {
					e.getValue().clear();
					e.getValue().add( Convert.toJson(row.get(e.getKey()).asNode()));
				}
			}
		}
		
		if (pending != null) {
			loadFromValueLists(pending, valuess, shorts);
			jc.consume(pending);
		}
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
	private static void multipleValueFetch(Map<String, List<JsonValue>> valuess, QuerySolution row) {
		for (Map.Entry<String, List<JsonValue>> e: valuess.entrySet()) {
			e.getValue().add( Convert.toJson(row.get(e.getKey()).asNode()));
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

	public static JsonArray convert(List<Aspect> aspects, List<QuerySolution> rows) {
		final JsonArray result = new JsonArray();
		JSONConsumer consumeToArray = new JSONConsumer() {
			@Override public void consume(JsonValue jo) { result.add(jo); }
		};
		convert(aspects, consumeToArray, rows.iterator() );
		return result;
	}
}
