/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Value;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestTranslateDataQuery {
	
	@Test public void testSingleEqualityFilter() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/prefixPart/").lock();
		Shortname sn = new Shortname( pm, "pre:post" );
		Filter f = new Filter(sn, Range.EQ(Value.wrap(17)));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		String sq = q.toSparql(p, pm);
		assertNoProblems(p);
		assertSameSparql( "PREFIX pre: <eh:/prefixPart/> SELECT ?item ?pre_post WHERE { ?item pre:post ?pre_post }", sq );
	}	
	
	@Test public void testDoubleEqualityFilter() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/prefixPart/").lock();
		Shortname snA = new Shortname( pm, "pre:A" );
		Shortname snB = new Shortname( pm, "pre:B" );
		Filter fA = new Filter(snA, Range.EQ(Value.wrap(8)));
		Filter fB = new Filter(snB, Range.EQ(Value.wrap(9)));
		List<Filter> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(filters);
	//
		String sq = q.toSparql(p, pm);
		assertNoProblems(p);
		assertSameSparql( "PREFIX pre: <eh:/prefixPart/> SELECT ?item ?pre_A ?pre_B WHERE { ?item pre:A ?pre_A. ?item pre:B ?pre_B }", sq );
	}
	
	private void assertNoProblems(Problems p) {
		if (p.size() > 0) fail("translation failed: " + p);
	}

	private void assertSameSparql(String expected, String toTest) {
		Query e = QueryFactory.create(expected);
		Query t = QueryFactory.create(toTest);
		assertEquals(e, t);
	}
}
