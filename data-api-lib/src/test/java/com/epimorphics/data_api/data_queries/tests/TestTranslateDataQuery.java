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
import com.epimorphics.data_api.data_queries.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

// TODO Apply DRY to the tests.
public class TestTranslateDataQuery {
	
	static final String SKOS = "http://www.w3.org/2004/02/skos/core";
	
	static final Aspect X = new TestAspects.MockAspect("eh:/mock-aspect/X");
	static final Aspect Y = new TestAspects.MockAspect("eh:/mock-aspect/Y");
	
	static final Aspect Yopt = new TestAspects.MockAspect("eh:/mock-aspect/Y").setIsOptional(true);

	PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("pre", "eh:/mock-aspect/")
//		.setNsPrefix("skos", SKOS)
		.lock()
		;
	
	@Test public void testUnfilteredSingleAspect() {
		Problems p = new Problems();
		List<Filter> filters = BunchLib.list();
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X }", sq );
	}		
	
	@Test public void testSingleEqualityFilter() {
		Problems p = new Problems();
		Shortname sn = new Shortname( pm, "pre:X" );
		Filter f = new Filter(sn, Range.EQ(Term.number(17)));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);		
		assertSameSelect
			( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 17)}"
			, sq 
			);
	}		
	
	@Test public void testSingleEqFilter() {
		testSingleFilterWithSpecifiedOp("eq", "=");
	}	
	
	@Test public void testSingleNeFilter() {
		testSingleFilterWithSpecifiedOp("ne", "!=");
	}	
	
	@Test public void testSingleLeFilter() {
		testSingleFilterWithSpecifiedOp("le", "<=");
	}	
	
	@Test public void testSingleLtFilter() {
		testSingleFilterWithSpecifiedOp("lt", "<");
	}	
	
	@Test public void testSingleGeFilter() {
		testSingleFilterWithSpecifiedOp("gt", ">");
	}	
	
	@Test public void testSingleGtFilter() {
		testSingleFilterWithSpecifiedOp("ge", ">=");
	}

	private void testSingleFilterWithSpecifiedOp(String opName, String opSparql) {	
		Problems p = new Problems();
		Shortname sn = new Shortname( pm, "pre:X" );
		Filter f = new Filter(sn, new Range(opName, BunchLib.list(Term.number(17))));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);		
		assertSameSelect
			( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X FILTER(?pre_X " + opSparql + " 17)}"
			, sq 
			);
	}
	
	@Test public void testSingleOneofFilter() {	
		Problems p = new Problems();
		Shortname sn = new Shortname( pm, "pre:X" );
		Filter f = new Filter(sn, new Range("oneof", BunchLib.list(Term.number(17), Term.number(99))));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);		
		assertSameSelect
			( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 17 || ?pre_X = 99)}"
			, sq 
			);
	}	
	
	@Test public void testSingleBelowFilter() {	
		Problems p = new Problems();
		Shortname sn = new Shortname( pm, "pre:X" );
		Node r = NodeFactory.createURI("eh:/prefixPart/stairs");
		Filter f = new Filter(sn, new Range("below", BunchLib.list(Term.URI(r.getURI()))));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSelect
			( "PREFIX pre: <eh:/mock-aspect/> PREFIX skos: <" + SKOS + "> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X . ?pre_X skos:broader <eh:/prefixPart/stairs> }"
			, sq 
			);
	}		
	
	@Test public void testSingleEqualityFilterWithUnfilteredAspect() {		
		Problems p = new Problems();
		Shortname sn = new Shortname( pm, "pre:X" );
		Filter f = new Filter(sn, Range.EQ(Term.number(17)));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X ?pre_Y WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 17). ?item pre:Y ?pre_Y}", sq );
	}		
	
	@Test public void testSingleEqualityFilterWithOptionalAspect() {
		Problems p = new Problems();
		Shortname sn = new Shortname( pm, "pre:X" );
		Filter f = new Filter(sn, Range.EQ(Term.number(17)));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X).include(Yopt);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X ?pre_Y WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 17). OPTIONAL {?item pre:Y ?pre_Y}}", sq );
	}		
	
	@Test public void testDoubleEqualityFilter() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/mock-aspect/").lock();
		Shortname snA = new Shortname( pm, "pre:X" );
		Shortname snB = new Shortname( pm, "pre:Y" );
		Filter fA = new Filter(snA, Range.EQ(Term.number(8)));
		Filter fB = new Filter(snB, Range.EQ(Term.number(9)));
		List<Filter> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, pm);
		assertNoProblems(p);
		assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X ?pre_Y WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 8). ?item pre:Y ?pre_Y FILTER(?pre_Y = 9)}", sq );
	}
	
	private void assertNoProblems(Problems p) {
		if (p.size() > 0) fail("translation failed: " + p.getProblemStrings());
	}

	private void assertSameSelect(String expected, String toTest) {
		Query e = QueryFactory.create(expected);
		Query t = QueryFactory.create(toTest);
		assertEquals(e, t);
	}
}
