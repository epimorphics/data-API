/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;

public class TestCompactionOptions {

	static final API_Dataset ds = 
		new API_Dataset(Setup.pseudoRoot(), null)
			.add(Setup.localAspect)
			;
	
	@Test public void testDefaultCompactionValues() {
		String incoming = "{}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		Asserts.assertNoProblems("should", p);
		assertEquals(false, q.suppressTypes());
		assertEquals(false, q.squeezeValues());
	}		
	
	@Test public void testSuppressTypesOptionFalse() {
		String incoming = "{'@suppress_types': false}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		Asserts.assertNoProblems("should parse @suppress_types", p);
		assertEquals(false, q.suppressTypes());
		assertEquals(false, q.squeezeValues());
	}	
	
	@Test public void testSuppressTypesOptionTrue() {
		String incoming = "{'@suppress_types': true}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		Asserts.assertNoProblems("should parse @suppress_types", p);
		assertEquals(true, q.suppressTypes());
		assertEquals(false, q.squeezeValues());
	}
	
	@Test public void testSqueezeValuesOptionFalse() {
		String incoming = "{'@compact_optionals': false}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		Asserts.assertNoProblems("should", p);
		assertEquals(false, q.suppressTypes());
		assertEquals(false, q.squeezeValues());
	}
	
	@Test public void testSqueezeValuesOptionTrue() {
		String incoming = "{'@compact_optionals': true}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		Asserts.assertNoProblems("should", p);
		assertEquals(false, q.suppressTypes());
		assertEquals(true, q.squeezeValues());
	}
	
	@Test public void testSuppressTypesAndSqueezeValuesOption() {
		String incoming = "{'@compact_optionals': true, '@suppress_types': true}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		Asserts.assertNoProblems("should", p);
		assertEquals(true, q.suppressTypes());
		assertEquals(true, q.squeezeValues());
	}
}
