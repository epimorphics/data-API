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
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
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
		String incoming = "{'pre:local': {'eq': 17}}";
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
	
	@Test public void testSingleFilterQueryForEQ() {
		testSingleFilterQueryWithNumericOp("eq");
	}
	
	@Test public void testSingleFilterQueryForNE() {
		testSingleFilterQueryWithNumericOp("ne");
	}
	
	@Test public void testSingleFilterQueryForLT() {
		testSingleFilterQueryWithNumericOp("lt");
	}
	
	@Test public void testSingleFilterQueryForLE() {
		testSingleFilterQueryWithNumericOp("le");
	}
	
	@Test public void testSingleFilterQueryForGE() {
		testSingleFilterQueryWithNumericOp("ge");
	}
	
	@Test public void testSingleFilterQueryForGT() {
		testSingleFilterQueryWithNumericOp("gt");
	}

	private void testSingleFilterQueryWithNumericOp(String op) {
		Shortname sn = new Shortname(pm, "pre:local");
		String incoming = "{'pre:local': {'" + op + "': 17}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, pm, jo);
//
		if (p.size() > 0) fail("problems detected in parser: " + p.getProblemStrings());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
//
		Range range = new Range(op, BunchLib.list(Value.wrap(new BigDecimal(17))));
		List<Filter> expected = BunchLib.list(new Filter( sn, range));		
		assertEquals(expected, q.filters());
	}
	
	@Test public void testSingleOneof() {
		Shortname sn = new Shortname(pm, "pre:local");
		String incoming = "{'pre:local': {'oneof': [17, 99]}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, pm, jo);
//
		if (p.size() > 0) fail("problems detected in parser: " + p.getProblemStrings());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
//
		Range range = new Range("oneof", BunchLib.list(Value.wrap(new BigDecimal(17)), Value.wrap(new BigDecimal(99))));
		List<Filter> expected = BunchLib.list(new Filter( sn, range));		
		assertEquals(expected, q.filters());
	}
	
	@Test public void testSingleBelow() {
		Shortname sn = new Shortname(pm, "pre:local");
		Node stairs = NodeFactory.createURI("pre:stairs");
		String incoming = "{'pre:local': {'below': {'@id': 'pre:stairs'}}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, pm, jo);
//
		if (p.size() > 0) fail("problems detected in parser: " + p.getProblemStrings());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
//
		Range range = new Range("below", BunchLib.list(Value.wrap(stairs)));
		List<Filter> expected = BunchLib.list(new Filter( sn, range));		
		assertEquals(expected, q.filters());
	}

}
