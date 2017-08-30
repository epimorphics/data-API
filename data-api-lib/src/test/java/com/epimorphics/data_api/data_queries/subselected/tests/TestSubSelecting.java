package com.epimorphics.data_api.data_queries.subselected.tests;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.parse_data_query.tests.Setup;
import com.epimorphics.data_api.reporting.Problems;

import org.junit.Test;
import static org.junit.Assert.*;

import static com.epimorphics.data_api.test_support.Asserts.*;

public class TestSubSelecting {	

	static final Aspect X = new TestAspects.MockAspect("eh:/prefixPart/X");

	static final Aspect Y = new TestAspects.MockAspect("eh:/prefixPart/Y")
		.setBelowPredicate("pre:child")
		;
	
	static final API_Dataset dsXY = new API_Dataset(Setup.pseudoRoot(), null).add(X).add(Y);
	
	@Test public void testSubSelectByItemLimit() {
		testSubSelectByItems("{'@limit': 2}", "LIMIT 2");
	}
	
	@Test public void testSubSelectByItemOffset() {
		testSubSelectByItems("{'@offset': 3}", "OFFSET 3");
	}
	
	@Test public void testSubSelectByItemOffsetAndLimit() {
		testSubSelectByItems("{'@offset': 4, '@limit': 5}", "LIMIT 5 OFFSET 4");
	}
	
	@Test public void testSubSelectByItemSort() {
		testSubSelectByItems("{'@sort': [{'@up': 'ZOG'}]}", "ORDER BY ?ZOG");
	}
	
	@Test public void testSubSelectByItemSortAndLimit() {
		testSubSelectByItems("{'@limit': 8, '@sort': [{'@up': 'ZOG'}]}", "ORDER BY ?ZOG LIMIT 8");
	}
	
	@Test public void testSubSelectByItemSortAndOffset() {
		testSubSelectByItems("{'@offset': 9, '@sort': [{'@up': 'ZOG'}]}", "ORDER BY ?ZOG OFFSET 9");
	}
	
	@Test public void testSubSelectByItemSortLimitAndOffset() {
		testSubSelectByItems("{'@limit': 10, '@offset': 9, '@sort': [{'@up': 'ZOG'}]}", "ORDER BY ?ZOG LIMIT 10 OFFSET 9");
	}

	private void testSubSelectByItems(String incoming, String modifiers) {
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery dq = DataQueryParser.Do(p, dsXY, jo);
		
		if (!p.isOK()) fail("could not parse: " + p.getProblemStrings());
		
		String toTest = dq.toSparql(p, dsXY); 
		
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, "{ SELECT ?item ?pre_X ?pre_Y"
			, "WHERE"
			, "{ ?item pre:X ?pre_X ."
			, "?item pre:Y ?pre_Y"
			, "}"
			, modifiers
			, "} }"
			);
		assertSameSelect(expected, toTest);
	}

	
	@Test public void testNotTiggeredByLimit() {
		testNotTriggeredBy("{'@limit': 10}", "LIMIT 10");
	}
	
	@Test public void testNotTiggeredByOffset() {
		testNotTriggeredBy("{'@offset': 20}", "OFFSET 20");
	}
	
	@Test public void testNotTiggeredByLimitAndOffset() {
		testNotTriggeredBy("{'@limit': 30, '@offset': 31}", "LIMIT 30 OFFSET 31");
	}
	
	@Test public void testNotTiggeredBySort() {
		testNotTriggeredBy("{'@sort': [{'@up': 'upwards'}]}", "ORDER BY ?upwards");
	}
	
	@Test public void testNotTiggeredBySortLimitOrOffset() {
		testNotTriggeredBy
			("{'@limit': 1, '@offset': 2, '@sort': [{'@up': 'upwards'}]}"
			, "ORDER BY ?upwards LIMIT 1 OFFSET 2"
			);
	}

	private void testNotTriggeredBy(String incoming, String modifiers) {
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery dq = DataQueryParser.Do(p, dsXY, jo);
		
		if (!p.isOK()) fail("could not parse: " + p.getProblemStrings());
		
		String toTest = dq.toSparql(p, dsXY); 
		
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, "  { SELECT ?item ?pre_X ?pre_Y WHERE { "
			, "?item pre:X ?pre_X ."
			, "?item pre:Y ?pre_Y"
			, "}"
			, modifiers
			, "}}"
			);	
		
		assertSameSelect(expected, toTest);
	}

}
