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

import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Value;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.libs.JSONLib;
import com.epimorphics.data_api.reporting.Problems;

public class TestParseDataQuery {
	
	@Test public void testEmptyQuery() {
		String incoming = "{}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		JSONLib.DataQuery q = DataQueryParser.Do(p, jo);
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
		assertTrue("expected no ranges in query.", q.ranges().isEmpty());
	}	
	
	@Test public void testSingleFilterQuery() {
		String incoming = "{'pre:local': {'op' : 'eq', 'operands': [17]}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		JSONLib.DataQuery q = DataQueryParser.Do(p, jo);
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
	//
		List<Range> expected = BunchLib.list(Range.EQ(Value.wrap(new BigDecimal(17))));		
		assertEquals(expected, q.ranges());
	}

}
