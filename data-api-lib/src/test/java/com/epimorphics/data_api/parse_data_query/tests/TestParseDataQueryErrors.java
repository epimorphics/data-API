/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;

public class TestParseDataQueryErrors {

	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null);

	@Test public void testUnknownShortname() {
		String incoming = "{'aaa:bbb': {'@eq': 'value'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		@SuppressWarnings("unused") DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertFalse(p.isOK());
		String messages = p.getProblemStrings();
		Asserts.assertContains( "aaa:bbb", messages );
		Asserts.assertContains( "unknown shortname", messages );
	}
	
	@Test public void testArrayRequiredForBooleans() {
		for (String key: "and/or/not".split("/"))
			for (String value: "{}/17/'hello'".split("/"))			
				testArrayRequiredForBoolean("@" + key, value);
		}
		
	private void testArrayRequiredForBoolean(String key, String value) {
		String incoming = "{'" + key + "': " + value + "}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		@SuppressWarnings("unused") DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertFalse(p.isOK());
		String messages = p.getProblemStrings();
		Asserts.assertContains( key, messages );
		Asserts.assertContains( "must be an array", messages );
	}
	
	@Test public void testNestedBooleanChecking() {
		String incoming = "{'@and': [{'@or': 17}, {'@and': {}}]}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		@SuppressWarnings("unused") DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertFalse(p.isOK());
		String messages = p.getProblemStrings();
		Asserts.assertContains( "must be an array", messages );
	}
	
	@Test public void testIllegalAspectOperand() {
		final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null);
		ds.add(new Aspect(ds.getPrefixes(), "pre:property"));
	//
		String incoming = "{'pre:property': 'value'}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		@SuppressWarnings("unused") DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertFalse(p.isOK());
		String messages = p.getProblemStrings();
		Asserts.assertContains( "pre:property", messages );
		Asserts.assertContains( "should be Object", messages );
	}
	
	@Test public void testSearchStringValue() {
		String incoming = "{'@search': 17}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		@SuppressWarnings("unused") DataQuery q = DataQueryParser.Do(p, ds, jo);		
		assertEquals(1, p.size());
		Asserts.assertContains("must be string", p.getProblemStrings());
		Asserts.assertContains("@search", p.getProblemStrings());
	}
}
