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
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;

public class TestBooleans {

	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
		.add(Setup.localAspect)
		;
	
	@Test public void testA() {
		String incoming = "{'@or': [{'pre:local': {'@eq': 1}}, {'pre:local': {'@eq': 2}}]}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		assertTrue(p.isOK());
		String generated = q.toSparql(p, ds);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_local"
			, "WHERE {"
			, "}"
			);
		System.err.println(">> " + generated );
		
		Asserts.assertSameSelect(expected, generated);
	//
	}
	
	@Test public void testB() {
		String incoming = "{'@and': [{'@or': [{'pre:local': {'@eq': 1}}, {'pre:local': {'@eq': 2}}]}]}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		assertTrue(p.isOK());
	//
	}	
}
