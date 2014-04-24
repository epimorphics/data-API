/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.end2end.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;

//
// TODO (here or elsewhere)
// oneof length offset 
// contains matches
// search
//

public class TestQueriesGetExpectedResults {

	App testapp;
    DSAPIManager man;
    
    @Before public void startup() throws IOException  {
		testapp = new App("testapp", new File("src/test/data/query-testing/test.conf"));
        man = testapp.getComponentAs("dsapi", DSAPIManager.class);
    }
    
    String allExpected = BunchLib.join
		( "["
		, "  {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A-resource'}]"
		, "  , 'eg:value': 17"
		, "  , 'eg:values': [17, 18, 19]"
		, "  , 'eg:label': [{'@lang': 'cy', '@value': 'A'}, 'A-one', 'A1']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B-resource'}]"
		, "  , 'eg:value': 18"
		, "  , 'eg:values': [42, 43]"
		, "  , 'eg:label': ['B-one', 'B1']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C-resource'}]"
		, "  , 'eg:value': 19"
		, "  , 'eg:values': [99]"
		, "  , 'eg:label': ['C-one', 'C1']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
		, "  , 'eg:value': 20"
		, "  , 'eg:values': [42, 43]"
		, "  , 'eg:label': ['D-two', 'D2']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
		, "  , 'eg:value': 21"
		, "  , 'eg:values': [42, 99]"
		, "  , 'eg:label': ['E', 'e']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F-resource'}]"
		, "  , 'eg:value': 22"
		, "  , 'eg:values': [42, 43]"
		, "  , 'eg:label': ['F', 'eff', {'@lang': 'cy', '@value': 'F'}, {'@lang': 'fr', '@value': 'f'}]"
		, "  }"
		, "]"
		);
    
    @Test public void testExpectAll() {    	
    	testQuery( "{}", allExpected );
    }
    
    @Test public void testExtractA() {    	
    	
    	String expectOnlyA = BunchLib.join
    		( "["
    		, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A-resource'}]"
    		, "  , 'eg:value': 17"
    		, "  , 'eg:values': [17, 18, 19]"
    		, "  , 'eg:label': [{'@lang': 'cy', '@value': 'A'}, 'A-one', 'A1']"
    		, "  }"
    		, "]"
    		);
    	
    	testQuery( "{'eg:value': {'@eq': 17}}", expectOnlyA );
    }
    
    @Test public void testExtractE() {    	
    	
    	String expectOnlyA = BunchLib.join
    		( "["
    		, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42, 99]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, "]"
    		);
    	
    	testQuery( "{'eg:value': {'@eq': 21}}", expectOnlyA );
    }
    
    @Test public void testExtractValues42() {    	
    	
    	String expectBDEandF = BunchLib.join
    		( "["
			, "  {"
			, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B'"
			, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B-resource'}]"
			, "  , 'eg:value': 18"
			, "  , 'eg:values': [42]"
			, "  , 'eg:label': ['B-one', 'B1']"
			, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F-resource'}]"
    		, "  , 'eg:value': 22"
    		, "  , 'eg:values': [42]"
    		, "  , 'eg:label': ['F', 'eff', {'@lang': 'cy', '@value': 'F'}, {'@lang': 'fr', '@value': 'f'}]"
    		, "  }"
    		, "]"
    		);
    	
    	testQuery( "{'eg:values': {'@eq': 42}}", expectBDEandF );
    }
    
    @Test public void testExtractValuesDEF_ByGE() {    	
    	
    	String expectDEF = BunchLib.join
    		( "["
			, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42, 99]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F-resource'}]"
    		, "  , 'eg:value': 22"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['F', 'eff', {'@lang': 'cy', '@value': 'F'}, {'@lang': 'fr', '@value': 'f'}]"
    		, "  }"
    		, "]"
    		);
    	
    	testQuery( "{'eg:value': {'@ge': 20}}", expectDEF );
    }    
    
    @Test public void testExtractABC_ByValueLt20() {
        String expectABC = BunchLib.join
    		( "["
    		, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A-resource'}]"
    		, "  , 'eg:value': 17"
    		, "  , 'eg:values': [17, 18, 19]"
    		, "  , 'eg:label': [{'@lang': 'cy', '@value': 'A'}, 'A-one', 'A1']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B-resource'}]"
    		, "  , 'eg:value': 18"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['B-one', 'B1']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C-resource'}]"
    		, "  , 'eg:value': 19"
    		, "  , 'eg:values': [99]"
    		, "  , 'eg:label': ['C-one', 'C1']"
    		, "  }"
    		, "]"
    		);
    	testQuery( "{'eg:value': {'@lt': 20}}", expectABC );
    }
    
    @Test public void testExtractAValueWithCombinedPredicates() {    	
    	String expectD = BunchLib.join
    		( "["
			, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, "]"
    		);
    	testQuery( "{'eg:value': {'@gt': 19, '@lt': 21}}", expectD );
    }
    
    @Test public void testExtractAValueWithANDedPredicates() {    	
    	String expectD = BunchLib.join
    		( "["
			, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, "]"
    		);
    	testQuery( "{'eg:value': {'@gt': 19}, '@and': [{'eg:value': {'@lt': 21}}]}", expectD);
    }
    
    @Test public void testSimpleOR() {    	
    	String expectDE = BunchLib.join
    		( "["
			, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42, 99]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, "]"
    		);
    	testQuery( "{'eg:value': {'@eq': 20}, '@or': [{'eg:value': {'@eq': 21}}]}", expectDE);
    }
    
    @Test public void testExtractNoValuesWithCombinedPredicates() {    	
    	String expectNone = "[]";
    	testQuery( "{'eg:value': {'@ge': 20, '@lt': 19}}", expectNone );
    }
    
    @Test public void testExtractNoValuesWithANDedCombinedPredicates() {    	
    	String expectNone = "[]";
    	testQuery( "{'eg:value': {'@ge': 20}, '@and': [{'eg:value': {'@lt': 19}}]}", expectNone );
    }
    
    @Test public void testExtractByTwoProperties() {
    	String expectD = BunchLib.join
        		( "["
				, "  {"
	    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
	    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
	    		, "  , 'eg:value': 20"
	    		, "  , 'eg:values': [43]"
	    		, "  , 'eg:label': ['D-two', 'D2']"
	    		, "  }"
	    		, "]"
	    		);
    	testQuery("{'eg:resource': {'@eq': {'@id': 'eg:DE-resource'}}, 'eg:values': {'@eq': 43}}", expectD);
    }
    
	public void testQuery(String queryString, String expectString) {
		JsonObject query = JSON.parse(queryString);
		JsonValue expectJSON = JSON.parse("{'array': " + expectString + " }").getAsObject().get("array");
		
		JSONWritable response = man.datasetDataEndpoint(null, "query-testing-dataset", query);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JSFullWriter jw = new JSFullWriter(bos) ;
		response.writeTo(jw);
		
		String results = bos.toString();
		String objectified = "{'array': " + results + "}\n";
		JsonObject jo = JSON.parse(objectified);
		JsonValue jv = jo.get("array");
		
//		System.err.println(">> GOT:\n" + jv);
		
//		JsonArray expectArray = expectJSON.getAsArray();
//		System.err.println( ">> got " + expectArray.size() + " results." );
//		for (JsonValue v: expectArray) {
//			System.err.println( ">>  " + v );
//		}
		
		Object expectObject = quasiCopyConvertingArraysToSets(expectJSON);
		Object resultObject = quasiCopyConvertingArraysToSets(jv);
		
//		System.err.println( ">> expectObject: " + expectObject );
//		System.err.println( ">> resultObject: " + resultObject );
		
//		System.err.println( ">> EXPECTED" );
		
//		for (Object x: ((HashSet<Object>) expectObject)) {
//			System.err.println( "->> " + x );
//		}
//		
//		System.err.println( ">> DERIVED" );
//		
//		for (Object x: ((HashSet<Object>) resultObject)) {
//			System.err.println( "->> " + x );
//		}
					
		assertEquals(expectObject, resultObject);
	}

	/**
	    Copy the JSON into a similar tree structure where JSONObjects
	    become maps and JSONArrays become sets. This allows the equality
	    test to ignore ordering within arrays, and since our results
	    all use arrays to represent sets, that's OK. 
	*/
	private Object quasiCopyConvertingArraysToSets(JsonValue jv) {
		if (jv.isArray()) {
			Set<Object> result = new HashSet<Object>();
			JsonArray ja = jv.getAsArray();
			for (int i = 0; i < ja.size(); i += 1) {
				result.add(quasiCopyConvertingArraysToSets(ja.get(i)));
			}
			return result;		
		} else if (jv.isObject()) {
			JsonObject jo = jv.getAsObject();
			Map<String, Object> new_jo = new HashMap<String, Object>();
			for (Map.Entry<String, JsonValue> e: jo.entrySet()) {
				new_jo.put(e.getKey(), quasiCopyConvertingArraysToSets(e.getValue()));
			}			
			return new_jo;
		} else {
			return jv;
		}
	}
	
	
}
