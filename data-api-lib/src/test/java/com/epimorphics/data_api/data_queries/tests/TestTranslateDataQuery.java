/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.aspects.tests.TestAspects;
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

// Apply DRY to the tests.
public class TestTranslateDataQuery {
	
	static final Aspect X = new TestAspects.MockAspect("eh:/mock-aspect/X");
	static final Aspect Y = new TestAspects.MockAspect("eh:/mock-aspect/Y");

	PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/mock-aspect/").lock();
	
	@Test public void testUnfilteredSingleAspect() {
		Problems p = new Problems();
		List<Filter> filters = BunchLib.list();
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSparql( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X }", sq );
	}		
	
	@Test public void testSingleEqualityFilter() {
		Problems p = new Problems();
		Shortname sn = new Shortname( pm, "pre:X" );
		Filter f = new Filter(sn, Range.EQ(Value.wrap(17)));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSparql( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 17)}", sq );
	}		
	
	@Test public void testSingleEqualityFilterWithUnfilteredAspect() {
		Problems p = new Problems();
		Shortname sn = new Shortname( pm, "pre:X" );
		Filter f = new Filter(sn, Range.EQ(Value.wrap(17)));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSparql( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X ?pre_Y WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 17). ?item pre:Y ?pre_Y}", sq );
	}		
	
	@Test public void testDoubleEqualityFilter() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/mock-aspect/").lock();
		Shortname snA = new Shortname( pm, "pre:X" );
		Shortname snB = new Shortname( pm, "pre:Y" );
		Filter fA = new Filter(snA, Range.EQ(Value.wrap(8)));
		Filter fB = new Filter(snB, Range.EQ(Value.wrap(9)));
		List<Filter> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSparql( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X ?pre_Y WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 8). ?item pre:Y ?pre_Y FILTER(?pre_Y = 9)}", sq );
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