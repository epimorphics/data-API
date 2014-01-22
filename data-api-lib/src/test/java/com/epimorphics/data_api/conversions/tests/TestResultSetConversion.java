/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Test;

import com.epimorphics.data_api.conversions.Convert;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.results.tests.TestTranslateQuerySolution;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestResultSetConversion {
	
	static final Model m = ModelFactory.createDefaultModel();
	
	static final Node itemA = m.createResource( "eh:/A" ).asNode();
	static final Node itemB = m.createResource( "eh:/B" ).asNode();
	
	static final Node value1 = m.createLiteral( "one" ).asNode();
	static final Node value2 = m.createLiteral( "two" ).asNode();
	static final Node value3 = m.createLiteral( "three" ).asNode();
	
	static final JsonValue itemA_json = Convert.objectWith("@id", itemA.getURI());
	static final JsonValue itemB_json = Convert.objectWith("@id", itemB.getURI());
	
	static final JsonValue value1_json = new JsonString("one");
	static final JsonValue value2_json = new JsonString("two");
	static final JsonValue value3_json = new JsonString("three");
	
	@Test public void testMe() {
		QuerySolution A = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "multiple", value1);
		QuerySolution B = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "multiple", value2);
		QuerySolution C = new TestTranslateQuerySolution.LocalQuerySolution("item", itemB, "multiple", value3);
		List<QuerySolution> x = BunchLib.list(A, B, C);
		JsonArray ja = convert(x);
		
		JsonArray expected = new JsonArray();
		
		JsonObject row1 = Convert.objectWith("item", itemA_json, "multiple", jsonArray(value1_json, value2_json));
		expected.add(row1);
		
		JsonObject row2 = Convert.objectWith("item", itemB_json, "multiple", jsonArray(value3_json));
		expected.add(row2);
		
		assertEquals(expected, ja);
	}

	private JsonArray jsonArray(JsonValue... elements) {
		JsonArray result = new JsonArray();
		for (JsonValue v: elements) result.add(v);
		return result;
	}

	private JsonArray convert(List<QuerySolution> rows) {
		JsonArray result = new JsonArray();
		
		List<String> vars = BunchLib.list("item", "multiple");
		
		Node current = null;
		JsonObject pending = null;
		
		for (QuerySolution row: rows) {
			
			Node item = row.get("item").asNode();
			
			if (item.equals(current)) {
				// more for this item
				pending.get("multiple").getAsArray().add(Convert.toJson(row.get("multiple").asNode()));
			} else {
				// new item, flush any existing item & reset current
				if (pending != null) result.add( pending );
				pending = Convert.toJson(vars, row);
				pending.put("multiple", jsonArray(pending.get("multiple")));
				current = item;
			}
		}
		
		if (pending != null) result.add(pending);
		
		return result;
	}

}
