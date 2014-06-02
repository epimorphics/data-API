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
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.SearchSpec;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;

public class TestTextSearching {
	
	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
		.add(Setup.localAspect)
		;

	@Test public void testSearchSetting() {
		String incoming = "{'@search': 'pattern'}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertEquals(0, p.size());
		assertEquals(BunchLib.list(new SearchSpec("pattern")), q.getSearchPatterns() );
	}
	
	@Test public void testSearchSettingFromAspect() {
		
		String incoming = "{'pre:local': {'@search': 'pattern'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		
		System.err.println(p.getProblemStrings());
		
		assertEquals(0, p.size());
		assertEquals(BunchLib.list(new SearchSpec("pattern", sn("pre:local"))), q.getSearchPatterns() );
	}
	
	@Test public void testSearchWithLimit() {
		Shortname property = sn("eh:/some.uri/");
		String incoming = "{'@search': {'@value': 'lookfor', '@limit': 17}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("failed to parse @search query", p);
	//
		String generated = q.toSparql(p, ds);
	//
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_local"
			, "WHERE {"
			, "  ?item <http://jena.apache.org/text#query> ('lookfor' 17) ."
			, "  ?item pre:local ?pre_local ."
			, "}"
			);
	//
		Asserts.assertSameSelect(expected, generated);
		}
	
	@Test public void testSearchWithLimitAndProperty() {
		String incoming = "{'@search': {'@value': 'lookfor', '@property': 'eh:/some.uri/', '@limit': 17}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("failed to parse @search query", p);
	//
		String generated = q.toSparql(p, ds);
	//
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_local"
			, "WHERE {"
			, "  ?item <http://jena.apache.org/text#query> (<eh:/some.uri/> 'lookfor' 17) ."
			, "  ?item pre:local ?pre_local ."
			, "}"
			);
	//
		Asserts.assertSameSelect(expected, generated);
		}
	
	
	private Shortname sn(String name) {
		return new Shortname(ds.getPrefixes(), name);
	}
	
	@Test public void testSearchSettingWithProperty() {
		Shortname property = sn("eh:/some.uri/");
		String incoming = "{'@search': {'@value': 'lookfor', '@property': 'eh:/some.uri/'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		assertEquals(0, p.size());
		assertEquals(BunchLib.list(new SearchSpec("lookfor", null, property)), q.getSearchPatterns() );
	}
	
	static final Aspect X = new TestAspects.MockAspect("eh:/prefixPart/X");
	static final Aspect Y = new TestAspects.MockAspect("eh:/prefixPart/Y");

	@Test public void testSearchOR() {		
		
		final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null).add(X).add(Y);
	
		String incoming = "{'pre:X': {'@search': 'A'}, '@or': [{'pre:Y': {'@search': 'B'}}]}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
	//
		Asserts.assertNoProblems("test data did not parse", p);
	//
		String generated = q.toSparql(p, ds);
	//
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y"
			, "{"
			, "  ?item pre:X ?pre_X ."
			, "  ?item pre:Y ?pre_Y ."
			, "{ SELECT ?item ?pre_X ?pre_Y {"
			, "  ?item pre:X ?pre_X ."
			, "  ?item pre:Y ?pre_Y ."
			, "  ?pre_Y <http://jena.apache.org/text#query> 'B' ."
		  	, "}}  UNION  {"
		  	, "SELECT ?item ?pre_X ?pre_Y {"
		  	, "  ?item pre:X ?pre_X ."
		  	, "  ?item pre:Y ?pre_Y ."
		  	, "  ?pre_X <http://jena.apache.org/text#query> 'A' ."
		  	, "}}"
		  	, "}"
			);
		Asserts.assertSameSelect(expected, generated);
	}
}
