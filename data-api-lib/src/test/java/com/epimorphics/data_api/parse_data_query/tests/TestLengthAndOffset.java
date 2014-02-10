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
import com.epimorphics.data_api.data_queries.Slice;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestLengthAndOffset {

	static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("pre",  "eh:/prefixPart/" )
		.lock()
		;

	@Test public void testLengthSetting() {
		String incoming = "{'@length': 17}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, pm, jo);
	//
		System.err.println(p.getProblemStrings());
		
		assertEquals(0, p.size());
		assertFalse(q.slice().isAll());
		
		assertEquals(Slice.create(new Integer(17)), q.slice());
		
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());	
	}
	
	@Test public void testOffsetSetting() {
		String incoming = "{'@offset': 42}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, pm, jo);
	//
		System.err.println(p.getProblemStrings());
		
		assertEquals(0, p.size());
		assertFalse(q.slice().isAll());
		
		assertEquals(Slice.create(null, new Integer(42)), q.slice());
		
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());	
	}
	
	@Test public void testLengthAlsoOffsetSetting() {
		String incoming = "{'@offset': 42, '@length': 60}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, pm, jo);
	//
		System.err.println(p.getProblemStrings());
		
		assertEquals(0, p.size());
		assertFalse(q.slice().isAll());
		
		assertEquals(Slice.create(new Integer(60), new Integer(42)), q.slice());
		
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());	
	}
}
