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
import com.epimorphics.data_api.data_queries.SearchSpec;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;

public class TestTextSearch {

	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
		.add(Setup.localAspect)
		;
	
	@Test public void testSearchSetting() {
		String incoming = "{'@search': 'pattern'}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertEquals(0, p.size());
		assertEquals(BunchLib.list(new SearchSpec(Aspect.NONE, "pattern")), q.getSearchPatterns() );
	}
	
	@Test public void testSearchSettingFromAspect() {
		
		String incoming = "{'pre:local': {'@search': 'pattern'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		
		System.err.println(p.getProblemStrings());
		
		assertEquals(0, p.size());
		Aspect local = aspect("pre:local");
		assertEquals(BunchLib.list(new SearchSpec(local, "pattern")), q.getSearchPatterns() );
	}
	
	private Shortname sn(String name) {
		return new Shortname(ds.getPrefixes(), name);
	}
	
	private Aspect aspect(String name) {
		return new Aspect(ds.getPrefixes(), name);
	}

	@Test public void testSearchSettingWithProperty() {
		Aspect a = aspect("eh:/some.uri/");
		Shortname property = a.getName(); // sn("eh:/some.uri/");
		String incoming = "{'@search': {'@value': 'lookfor', '@property': 'eh:/some.uri/'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		assertEquals(0, p.size());
		assertEquals(BunchLib.list(new SearchSpec(Aspect.NONE, "lookfor", property)), q.getSearchPatterns() );
	}
	
	@Test public void testSearchWithJustLimit() {
		String incoming = "{'@search': {'@value': 'lookfor', '@limit': 17}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("failed to parse search", p);
		assertEquals(BunchLib.list(new SearchSpec(Aspect.NONE, "lookfor", null, 17)), q.getSearchPatterns() );
	}
	
	@Test public void testSearchWithLimitAndProperty() {
		Shortname property = sn("eh:/some.uri/");
		String incoming = "{'@search': {'@value': 'lookfor', '@limit': 17, '@property': 'eh:/some.uri/'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("failed to parse search", p);
		assertEquals(BunchLib.list(new SearchSpec(Aspect.NONE, "lookfor", property, 17)), q.getSearchPatterns() );
	}
	
	@Test public void testSearchWithNeitherLimitNotPropertyRaisesProblem() {
		String incoming = "{'@search': {'@value': 'lookfor'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery _ignored = DataQueryParser.Do(p, ds, jo);		
		assertFalse("failed to detect missing-both-property-and-limit error", p.isOK());
		Asserts.assertInsensitiveContains("neither @property nor @limit", p.getProblemStrings());
	}
}
