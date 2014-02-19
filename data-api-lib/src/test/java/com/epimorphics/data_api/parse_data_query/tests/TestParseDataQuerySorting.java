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
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Sort;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestParseDataQuerySorting {

	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null);
	
	@Test public void testEmptyQuery() {
		String incoming = "{}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
		assertTrue("expected no filters in query.", q.filters().isEmpty());		
	}
	
	@Test public void testEmptySortList() {
		String incoming = "{'@sort': []}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
		assertTrue("expected no filters in query.", q.filters().isEmpty());		
	}
	
	@Test public void testSingleSort() {
		String incoming = "{'@sort': [{'@up': 'a:b'}]}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		PrefixMapping pm = ds.getPrefixes();
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no filters in query.", q.filters().isEmpty());		
	//
		Shortname sn = new Shortname(pm, "a:b");
		Sort expected = new Sort(sn, true);
		assertEquals( BunchLib.list(expected), q.sorts());
	}
	
	@Test public void testMultipleSorts() {
		String incoming = "{'@sort': [{'@up': 'a:b'}, {'@down': 'x:y'}]}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		PrefixMapping pm = ds.getPrefixes();
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no filters in query.", q.filters().isEmpty());		
	//
		Shortname ab_sn = new Shortname(pm, "a:b");
		Shortname xy_sn = new Shortname(pm, "x:y");
		Sort ab = new Sort(ab_sn, true), xy = new Sort(xy_sn, false);
		assertEquals( BunchLib.list(ab, xy), q.sorts());
	}
	
}
