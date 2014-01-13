/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Value;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestParseDataQuery {
	
	@Test public void testEmptyQuery() {
		String incoming = "{}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, pm, jo);
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
		assertTrue("expected no filters in query.", q.filters().isEmpty());
	}	
	
	static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("pre",  "eh:/prefixPart/" )
		.lock()
		;
	
	@Test public void testSingleFilterQuery() {
		Shortname sn = new Shortname(pm, "pre:local");
		String incoming = "{'pre:local': {'op' : 'eq', 'operands': [17]}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, pm, jo);
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
	//
		List<Filter> expected = BunchLib.list(new Filter( sn, Range.EQ(Value.wrap(new BigDecimal(17)))));		
		assertEquals(expected, q.filters());
	}

}
