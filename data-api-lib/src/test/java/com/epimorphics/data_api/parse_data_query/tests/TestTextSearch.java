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
import com.epimorphics.data_api.data_queries.SearchSpec;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;

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
		assertEquals(BunchLib.list(new SearchSpec("lookfor", property)), q.getSearchPatterns() );
	}

}
