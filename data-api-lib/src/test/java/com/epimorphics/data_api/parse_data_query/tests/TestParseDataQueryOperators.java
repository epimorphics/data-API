/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.SearchSpec;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestParseDataQueryOperators {
	
	static final API_Dataset ds = 
		new API_Dataset(Setup.pseudoRoot(), null)
			.add(Setup.localAspect)
			;
	
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
	
	static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("pre",  "eh:/prefixPart/" )
		.lock()
		;

	@Test public void testSingleFilterQuery() {
		Shortname sn = new Shortname(pm, "pre:local");
		String incoming = "{'pre:local': {'@eq': 17}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
	//
		assertEquals(0, p.size());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
	//
		List<Filter> expected = BunchLib.list(new Filter( sn, Range.EQ(Term.number(new BigDecimal(17)))));		
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
		String incoming = "{'pre:local': {'@" + op + "': 17}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
//
		if (p.size() > 0) fail("problems detected in parser: " + p.getProblemStrings());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
//
		Range range = new Range(op, BunchLib.list(Term.number(new BigDecimal(17))));
		List<Filter> expected = BunchLib.list(new Filter( sn, range));		
		assertEquals(expected, q.filters());
	}
	
	@Test public void testSingleOneof() {
		testSingleOperator("oneof", "[17, 99]", Term.number(new BigDecimal(17)), Term.number(new BigDecimal(99)) );
	}
	
	@Test public void testSingleMatches() {
		testSingleOperator("matches", "'reg.*exp'", Term.string("reg.*exp"));
	}
	
	@Test public void testSingleContains() {
		testSingleOperator("contains", "'substring'", Term.string("substring") );
	}
	
	// @search no longer generates filters; search objects are
	// handled separately.
	@Test public void testSingleSearch() {
		Term[] values = { Term.string("texty bits") };
		Shortname sn = new Shortname(pm, "pre:local");
		String incoming = "{'pre:local': {'@search': 'texty bits'}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
//
		if (p.size() > 0) fail("problems detected in parser: " + p.getProblemStrings());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
//
		assertEquals(BunchLib.list(), q.filters());
		assertEquals(BunchLib.list(new SearchSpec("texty bits", sn)), q.getSearchPatterns() );
	}
	
	@Test public void testSingleBelow() {
		testSingleOperator("below", "{'@id': 'eh:/resource'}", Term.URI("eh:/resource") );
	}
		
	void testSingleOperator(String op, String operand, Term...values) {
		Shortname sn = new Shortname(pm, "pre:local");
		String incoming = "{'pre:local': {'@_OP': _ARGS}}"
			.replaceAll("_OP", op)
			.replaceAll("_ARGS", operand)
			;
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
//
		if (p.size() > 0) fail("problems detected in parser: " + p.getProblemStrings());
		assertTrue(q.slice().isAll());
		assertNull(q.lang());
		assertTrue("expected no sorts in query.", q.sorts().isEmpty());
//
		Range range = new Range(op, Arrays.asList(values));
		List<Filter> expected = BunchLib.list(new Filter( sn, range));		
		assertEquals(expected, q.filters());
	}

}
