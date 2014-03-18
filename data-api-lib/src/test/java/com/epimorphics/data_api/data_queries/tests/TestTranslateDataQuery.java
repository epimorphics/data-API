/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;


import static com.epimorphics.data_api.test_support.Asserts.assertNoProblems;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Guard;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.SearchSpec;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Slice;
import com.epimorphics.data_api.data_queries.Sort;
import com.epimorphics.data_api.data_queries.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.shared.PrefixMapping;

// TODO Apply DRY to the tests.
public class TestTranslateDataQuery {

    static PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("pre", "eh:/mock-aspect/")
		.setNsPrefix("skos", SKOS.getURI())
		.lock()
		;
	
	static final Aspect X = new TestAspects.MockAspect("eh:/mock-aspect/X");
	
	static final Aspect Y = new TestAspects.MockAspect("eh:/mock-aspect/Y")
		.setBelowPredicate("pre:child")
		;
	
	static final Aspect Yopt = new TestAspects.MockAspect("eh:/mock-aspect/Y").setIsOptional(true);

	
	@Test public void testUnfilteredSingleAspect() {
		Problems p = new Problems();
		List<Filter> filters = BunchLib.list();
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		Asserts.assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X }", sq );
	}		
	
	@Test public void testSingleSort() {
		Problems p = new Problems();
		List<Filter> filters = BunchLib.list();
		List<Sort> sorts = BunchLib.list(new Sort(new Shortname(pm, "pre:X"), true));
		DataQuery q = new DataQuery(filters, sorts);
	//
		Aspects a = new Aspects().include(X);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		Asserts.assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X } ORDER BY ?pre_X", sq );
	}	
	
	@Test public void testMultipleSorts() {
		Problems p = new Problems();
		List<Filter> filters = BunchLib.list();
		List<Sort> sorts = BunchLib.list
			( new Sort(new Shortname(pm, "pre:X"), true)
			, new Sort(new Shortname(pm, "pre:Y"), false)
			);
		DataQuery q = new DataQuery(filters, sorts);
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		
		Asserts.assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X ?pre_Y WHERE { ?item pre:X ?pre_X . ?item pre:Y ?pre_Y } ORDER BY ?pre_X DESC(?pre_Y)", sq );
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
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);		
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X WHERE {"
			, "?item pre:X 17 BIND(17 AS?pre_X)}"
			);
		Asserts.assertSameSelect( expected, sq );
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
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);	
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X WHERE"
			, "{"
			, (opName.equals("eq") ? "?item pre:X 17 BIND(17 AS ?pre_X)" : ("?item pre:X ?pre_X FILTER(?pre_X " + opSparql + " 17)"))
			, "}"
			);
		Asserts.assertSameSelect( expected, sq );
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
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);		
		Asserts.assertSameSelect
			( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X WHERE { ?item pre:X ?pre_X FILTER(?pre_X = 17 || ?pre_X = 99)}"
			, sq 
			);
	}	
	
	@Test public void testSingleXBelowFilter() {
		testSingleSimpleFilter
			( X
			, "below"
			, Term.URI("eh:/prefixPart/stairs")
			, "<eh:/prefixPart/stairs> skos:narrower* ?pre_X"
			);
	}	
	
	@Test public void testSingleYBelowFilter() {
		testSingleSimpleFilter
			( Y
			, "below"
			, Term.URI("eh:/prefixPart/stairs")
			, "<eh:/prefixPart/stairs> pre:child* ?pre_Y"
			);
	}
	
	@Test public void testSingleContainsFilter() {
		testSingleSimpleFilter
			( X
			, "contains"
			, Term.string("substring")
			, "FILTER(CONTAINS(?pre_X, 'substring'))"
			);
	}
	
	@Test public void testSingleMatchesFilter() {
		testSingleSimpleFilter
			( X
			, "matches"
			, Term.string("alpha.*beta")
			, "FILTER(REGEX(?pre_X, 'alpha.*beta'))"
			);
	}
	
	@Test public void testSingleSearchFilter() {
		testSingleSimpleFilter
			( X
			, "search"
			, Term.string("look for me")
			, "?pre_X <http://jena.apache.org/text#query> 'look for me'"
			);
	}
	
	private void testSingleSimpleFilter(Aspect useAspect, String op, Term term, String filter) {
		Shortname sn = useAspect.getName();
		Problems p = new Problems();		
		Filter f = new Filter(sn, new Range(op, BunchLib.list(term)));
		List<Filter> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(filters);
	//
		Aspects a = new Aspects().include(useAspect);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		
		String var = "?" + useAspect.asVar();
		String prop = useAspect.getName().getCURIE();
		
		String prefix_p = "PREFIX pre: <eh:/mock-aspect/>\n";
		String prefix_skos = (op.equals("below") ? "PREFIX skos: <" + SKOS.getURI() + "> " : "");
		String select = "SELECT ?item _VAR WHERE { ?item _PROP _VAR . " + filter + " }";
		
		String expected = 
			prefix_p + (select.contains("skos:") ? prefix_skos : "") 
			+ select.replaceAll("_VAR", var).replaceAll("_PROP", prop )
			;
				
		Asserts.assertSameSelect( expected, sq	);
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
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, "?item pre:X 17 BIND(17 AS ?pre_X)"
			, ". ?item pre:Y ?pre_Y}"
			);
		Asserts.assertSameSelect( expected, sq );
	}			
	
	@Test public void testGlobalSearch() {		
		Problems p = new Problems();
		DataQuery q = new DataQuery
			( new ArrayList<Filter>()
			, new ArrayList<Sort>()
			, null // new ArrayList<Guard>()
			, Slice.all()
			, BunchLib.list( new SearchSpec("look for me") )
			);
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X ?pre_Y"
			, "WHERE {"
			, "?item <http://jena.apache.org/text#query> 'look for me'."
			, "?item pre:X ?pre_X."
			, "?item pre:Y ?pre_Y"
			, "}"
			);
		
		Asserts.assertSameSelect( expected, sq );
	}		
	
	@Test public void testGlobalSearchWithProperty() {		
		Problems p = new Problems();
		Shortname someProperty = new Shortname(pm, "pre:someProperty");
		Shortname otherProperty = new Shortname(pm, "pre:otherProperty");
		DataQuery q = new DataQuery
			( new ArrayList<Filter>()
			, new ArrayList<Sort>()
			, new ArrayList<Guard>()
			, Slice.all()
			, BunchLib.list( new SearchSpec("look for me", someProperty, otherProperty) )
			);
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X ?pre_Y"
			, "WHERE {"
			, "?pre_someProperty <http://jena.apache.org/text#query> (pre:otherProperty 'look for me')."
			, "?item pre:X ?pre_X."
			, "?item pre:Y ?pre_Y"
			, "}"
			);
		
		Asserts.assertSameSelect( expected, sq );
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
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		Asserts.assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_X ?pre_Y WHERE { ?item pre:X 17 BIND(17 AS ?pre_X). OPTIONAL {?item pre:Y ?pre_Y}}", sq );
	}		

	@Test public void testLengthCopied() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/mock-aspect/").lock();
		Shortname snA = new Shortname( pm, "pre:X" );
		Shortname snB = new Shortname( pm, "pre:Y" );
		Filter fA = new Filter(snA, Range.EQ(Term.number(8)));
		Filter fB = new Filter(snB, Range.EQ(Term.number(9)));
		List<Filter> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(filters, new ArrayList<Sort>(), Slice.create(17));
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		String expect = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X ?pre_Y"
			, "WHERE { ?item pre:X 8 BIND(8 AS ?pre_X). ?item pre:Y 9 BIND(9 AS ?pre_Y)}"
			, "LIMIT 17"
			);
		Asserts.assertSameSelect(expect, sq );
	}
	
	@Test public void testOffsetCopied() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/mock-aspect/").lock();
		Shortname snA = new Shortname( pm, "pre:X" );
		Shortname snB = new Shortname( pm, "pre:Y" );
		Filter fA = new Filter(snA, Range.EQ(Term.number(8)));
		Filter fB = new Filter(snB, Range.EQ(Term.number(9)));
		List<Filter> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(filters, new ArrayList<Sort>(), Slice.create(null, 1066));
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		String expect = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X ?pre_Y"
			, "WHERE { ?item pre:X 8 BIND(8 AS ?pre_X). ?item pre:Y 9 BIND(9 AS ?pre_Y)}"
			, "OFFSET 1066"
			);
		Asserts.assertSameSelect(expect, sq );
	}
	
	@Test public void testLengthAndOffsetCopied() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/mock-aspect/").lock();
		Shortname snA = new Shortname( pm, "pre:X" );
		Shortname snB = new Shortname( pm, "pre:Y" );
		Filter fA = new Filter(snA, Range.EQ(Term.number(8)));
		Filter fB = new Filter(snB, Range.EQ(Term.number(9)));
		List<Filter> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(filters, new ArrayList<Sort>(), Slice.create(17, 1829));
	//
		Aspects a = new Aspects().include(X).include(Y);
	//
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		String expect = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X ?pre_Y"
			, "WHERE { ?item pre:X 8 BIND(8 AS ?pre_X). ?item pre:Y 9 BIND(9 AS ?pre_Y)}"
			, "LIMIT 17 OFFSET 1829"
			);
		Asserts.assertSameSelect(expect, sq );
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
		String sq = q.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/mock-aspect/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE { "
			, "?item pre:X 8 BIND(8 AS ?pre_X)"
			, ". ?item pre:Y 9 BIND(9 AS ?pre_Y)}"
			);
		Asserts.assertSameSelect( expected, sq );
	}
	
	@Test public void testDatasetRestriction() {
		Problems p = new Problems();
		DataQuery q = new DataQuery(BunchLib.<Filter>list());
	//
		Aspects a = new Aspects();
	//
		String sq = q.toSparql(p, a, "?item pre:has pre:value", pm);
		Asserts.assertNoProblems("translation failed", p);
		Asserts.assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item WHERE { { ?item pre:has pre:value } }", sq );
		}
	
	@Test public void testDatasetRestrictionWithAspects() {
		Problems p = new Problems();
		DataQuery q = new DataQuery(BunchLib.<Filter>list());
	//
		Aspects a = new Aspects().include(Y);
	//
		String sq = q.toSparql(p, a, "?item pre:has pre:value .", pm);
		assertNoProblems("translation failed", p);
		Asserts.assertSameSelect( "PREFIX pre: <eh:/mock-aspect/> SELECT ?item ?pre_Y WHERE { { ?item pre:has pre:value . } ?item pre:Y ?pre_Y }", sq );
		}
}
