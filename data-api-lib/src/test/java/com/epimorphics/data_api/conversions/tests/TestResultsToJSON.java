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

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.conversions.Convert;
import com.epimorphics.data_api.conversions.ResultsToJson;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.libs.JSONLib;
import com.epimorphics.data_api.results.tests.TestTranslateQuerySolution;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestResultsToJSON {
	
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
		QuerySolution A = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_multiple", value1);
		QuerySolution B = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_multiple", value2);
		QuerySolution C = new TestTranslateQuerySolution.LocalQuerySolution("item", itemB, "pre_multiple", value3);
		List<QuerySolution> x = BunchLib.list(A, B, C);
		
		Aspect multiple = new TestAspects.MockAspect( "eh:/aspect/multiple" ).setIsMultiValued(true);
		List<Aspect> aspects = BunchLib.list( multiple );
				
		List<ResultsToJson.Row> ja = ResultsToJson.convert(aspects, x);
		
		JsonArray expected = new JsonArray();
		
		JsonObject row1 = Convert.objectWith("item", itemA_json, "pre:multiple", JSONLib.jsonArray(value1_json, value2_json));
		expected.add(row1);
		
		JsonObject row2 = Convert.objectWith("item", itemB_json, "pre:multiple", JSONLib.jsonArray(value3_json));
		expected.add(row2);
		
		assertEquals(expected, ja);
	}
	
	

}
