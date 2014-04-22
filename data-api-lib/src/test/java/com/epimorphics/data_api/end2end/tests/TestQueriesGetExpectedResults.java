/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.end2end.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.jena.atlas.json.JSON;
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

public class TestQueriesGetExpectedResults {

	App testapp;
    DSAPIManager man;
    
    @Before public void startup() throws IOException  {
		testapp = new App("testapp", new File("src/test/data/query-testing/test.conf"));
        man = testapp.getComponentAs("dsapi", DSAPIManager.class);
    }
    
    @Test public void test0() {
    	
    	String expected = BunchLib.join
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
    	
    	testQuery( "{}", expected );
    	
    }
    
	public void testQuery(String queryString, String resultString) {
		JsonObject query = JSON.parse(queryString);
		JsonValue result = JSON.parse("{'array': " + resultString + " }").getAsObject().get("array");
		
		JSONWritable response = man.datasetDataEndpoint(null, "query-testing-dataset", query);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JSFullWriter jw = new JSFullWriter(bos) ;
		response.writeTo(jw);
		
		String results = bos.toString();
		String objectified = "{'array': " + results + "}\n";
		JsonObject jo = JSON.parse(objectified);
		JsonValue jv = jo.get("array");
//		for (JsonValue row: jv.getAsArray()) {
//			System.err.println( ">> " + row );
//		}
		assertEquals(result, jv);
	}
	
	
}
