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
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;

public class TestParseDataQueryErrors {

	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null);

	@Test public void testUnknownShortname() {
		String incoming = "{'aaa:bbb': {'@eq': 'value'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		@SuppressWarnings("unused") DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertFalse(p.isOK());
		String messages = p.getProblemStrings();
		assertContains( "aaa:bbb", messages );
		assertContains( "unknown shortname", messages );
	}
	
	@Test public void testIllegalAspectOperand() {
		final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null);
		ds.add(new Aspect("eh:/prefixPart/property", new Shortname(ds.getPrefixes(), "pre:property")));
	//
		String incoming = "{'pre:property': 'value'}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		@SuppressWarnings("unused") DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertFalse(p.isOK());
		String messages = p.getProblemStrings();
		assertContains( "pre:property", messages );
		assertContains( "should be Object", messages );
	}

	private void assertContains(String expected, String s) {
		if (!s.toLowerCase().contains(expected.toLowerCase()))
			fail("'" + expected + "' was not present in '" + s + "'");		
	}
}
