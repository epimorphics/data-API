/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import static com.epimorphics.data_api.test_support.Asserts.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.config.DefaultPrefixes;
import com.epimorphics.data_api.data_queries.Constraint;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Guard;
import com.epimorphics.data_api.data_queries.Modifiers;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.SearchSpec;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Slice;
import com.epimorphics.data_api.data_queries.Sort;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.end2end.tests.QueryTestSupport;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.parse_data_query.tests.Setup;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;
import com.epimorphics.vocabs.SKOS;
import org.apache.jena.shared.PrefixMapping;

// TODO Apply DRY to the tests.
public class TestTranslateDataQuery {

    static PrefixMapping pm = PrefixMapping.Factory.create()
    	.setNsPrefixes(DefaultPrefixes.get())
		.setNsPrefix("pre", Setup.pm.getNsPrefixURI("pre"))
		.setNsPrefix("skos", SKOS.getURI())
		.lock()
		;
	
	static final Aspect X = new TestAspects.MockAspect("eh:/prefixPart/X");
	
	static final Aspect LOC = new TestAspects.MockAspect("eh:/prefixPart/local");
	
	static final Aspect Y = new TestAspects.MockAspect("eh:/prefixPart/Y")
		.setBelowPredicate("pre:child")
		;
	
	static final Aspect Yopt = new TestAspects.MockAspect("eh:/prefixPart/Y").setIsOptional(true);

	static final API_Dataset dsX = new API_Dataset(Setup.pseudoRoot(), null).add(X);
	
	static final API_Dataset dsLOC = new API_Dataset(Setup.pseudoRoot(), null).add(LOC);
	
	static final API_Dataset dsXY = new API_Dataset(Setup.pseudoRoot(), null).add(X).add(Y);
	
	@Test public void testUnfilteredSingleAspect() {
		Problems p = new Problems();
		List<Constraint> filters = BunchLib.list();
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//
		String sq = q.toSparql(p, dsX);
		assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X WHERE {"
			, "?item pre:X ?pre_X ."
			, "}"
			);
		assertSameSelect( expected, sq );
	}		
	
	@Test public void testSingleSort() {
		Problems p = new Problems();
		List<Constraint> filters = BunchLib.list();
		List<Sort> sorts = BunchLib.list(new Sort(new Shortname(pm, "pre:X"), true));
		DataQuery q = new DataQuery(Constraint.filters(filters), sorts);
	//
		String sq = q.toSparql(p, dsX);
		assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X"
			, "WHERE { "
			, "{ SELECT ?item ?pre_X {"
			, "?item pre:X ?pre_X"
			, "}"
			, "ORDER BY ?pre_X"
			, "}}"
			);
		assertSameSelect( expected, sq );
	}	
	
	@Test public void testMultipleConflictingEqualities() {
		Problems p = new Problems();
		JsonObject query = JSON.parse("{'@and': [{'pre:X': {'@eq': 1}}, {'pre:X': {'@eq': 2}}]}");
		DataQuery dq = DataQueryParser.Do(p, dsXY, query);
		String sq = dq.toSparql(p, dsXY);
		
		// sq = sq.replaceAll("[ \n]+", " ");
		
		Asserts.denyContains("BIND", sq);
		assertContains("?item pre:X ?pre_X .", sq);
		assertContains("FILTER(?pre_X = 1)", sq);
		assertContains("FILTER(?pre_X = 2)", sq);
	}

	@Test public void testMultipleSorts() {
		Problems p = new Problems();
		List<Constraint> filters = BunchLib.list();
		List<Sort> sorts = BunchLib.list
			( new Sort(new Shortname(pm, "pre:X"), true)
			, new Sort(new Shortname(pm, "pre:Y"), false)
			);
		DataQuery q = new DataQuery(Constraint.filters(filters), sorts);
	//
		String sq = q.toSparql(p, dsXY);
		assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, "{ SELECT ?item ?pre_X ?pre_Y WHERE {"
			, " ?item pre:X ?pre_X ."
			, " ?item pre:Y ?pre_Y ."
			, "}"
			, "ORDER BY ?pre_X DESC(?pre_Y)"
			, "}}"
			);
		assertSameSelect( expected, sq );
	}
		
	@Test public void testSingleEqualityFilter() {
		Problems p = new Problems();
		Aspect sn = new Aspect( pm, "pre:X" );
		Constraint f = new Filter(sn, Range.EQ(Term.number(17)));
		List<Constraint> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//
		String sq = q.toSparql(p, dsX);	
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X WHERE {"
			, " ?item pre:X ?pre_X ."
			, "FILTER(?pre_X = 17)"
			, "}"
			);
		Asserts.assertSameSelect( expected, sq );
	}		
	
	@Test public void testSingleEqFilter() {
		testSingleFilterWithSpecifiedOp(Operator.EQ, "=");
	}	
	
	@Test public void testSingleNeFilter() {
		testSingleFilterWithSpecifiedOp(Operator.NE, "!=");
	}	
	
	@Test public void testSingleLeFilter() {
		testSingleFilterWithSpecifiedOp(Operator.LE, "<=");
	}	
	
	@Test public void testSingleLtFilter() {
		testSingleFilterWithSpecifiedOp(Operator.LT, "<");
	}	
	
	@Test public void testSingleGeFilter() {
		testSingleFilterWithSpecifiedOp(Operator.GT, ">");
	}	
	
	@Test public void testSingleGtFilter() {
		testSingleFilterWithSpecifiedOp(Operator.GE, ">=");
	}

	private void testSingleFilterWithSpecifiedOp(Operator op, String opSparql) {	
		Problems p = new Problems();
		Aspect sn = new Aspect( pm, "pre:X" );
		Constraint f = new Filter(sn, new Range(op, BunchLib.list(Term.number(17))));
		List<Constraint> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//
//		API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null).add(X);
		String sq = q.toSparql(p, dsX);
	//
		Asserts.assertNoProblems("translation failed", p);	
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X WHERE"
			, "{"
			, " ?item pre:X ?pre_X . FILTER(?pre_X " + opSparql + " 17)"
			, "}"
			);
		Asserts.assertSameSelect( expected, sq );
	}
	
	@Test public void testSingleOneofFilter() {	
		Problems p = new Problems();
		Aspect sn = new Aspect( pm, "pre:X" );
		Constraint f = new Filter(sn, new Range(Operator.ONEOF, BunchLib.list(Term.number(17), Term.number(99))));
		List<Constraint> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//
		String sq = q.toSparql(p, dsX);
		assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X WHERE {"
			, " ?item pre:X ?pre_X .", "FILTER( (?pre_X = 17) || (?pre_X = 99))"
			, "}"
			);
		assertSameSelect( expected, sq );
	}	
	
//	@Test public void testSingleXBelowFilter() {
//		testSingleSimpleFilter
//			( X
//			, Operator.BELOW
//			, Term.URI("eh:/prefixPart/stairs")
//			, "<eh:/prefixPart/stairs> skos:narrower* ?pre_X"
//			);
//	}	
//	
//	@Test public void testSingleYBelowFilter() {
//		testSingleSimpleFilter
//			( Y
//			, Operator.BELOW
//			, Term.URI("eh:/prefixPart/stairs")
//			, "<eh:/prefixPart/stairs> pre:child* ?pre_Y"
//			);
//	}
	
	@Test public void testSingleContainsFilter() {
		testSingleSimpleFilter
			( X
			, Operator.CONTAINS
			, Term.string("substring")
			, "FILTER(CONTAINS(?pre_X, 'substring'))"
			);
	}
	
	@Test public void testSingleMatchesFilter() {
		testSingleSimpleFilter
			( X
			, Operator.MATCHES
			, Term.string("alpha.*beta")
			, "FILTER(REGEX(?pre_X, 'alpha.*beta'))"
			);
	}
	
	@Test public void testSingleSearchFilter() {
		Problems p = new Problems();		
		List<Constraint> filters = BunchLib.list();
		SearchSpec s = new SearchSpec(Aspect.NONE, "look for me");
		List<SearchSpec> searches = BunchLib.list(s);
		DataQuery q = new DataQuery(Constraint.filters(filters, searches));
	//	
		String sq = q.toSparql(p, dsX);
		assertNoProblems("translation failed", p);
	//
		String expected = BunchLib.join
			( "PREFIX text: <http://jena.apache.org/text#>"
			, "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X WHERE {"
			, "  ?item text:query 'look for me' . "
			, "  ?item pre:X ?pre_X . "
			, "}"
			);
		
		assertSameSelect( expected, sq	);
	}
	
	private void testSingleSimpleFilter(Aspect useAspect, Operator op, Term term, String filter) {
		Aspect sn = useAspect;
		Problems p = new Problems();		
		Constraint f = new Filter(sn, new Range(op, BunchLib.list(term)));
		List<Constraint> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//	
		API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
			.add(useAspect)
			;
		
		String sq = q.toSparql(p, ds);
		assertNoProblems("translation failed", p);
		
		String var = useAspect.asVar();
		String prop = useAspect.getName().getCURIE();
		
		String prefix_p = "PREFIX pre: <eh:/prefixPart/>\n";
		String prefix_skos = (op.equals(Operator.BELOW) ? "PREFIX skos: <" + SKOS.getURI() + "> " : "");
		String select = "SELECT ?item _VAR WHERE {"
				+ "?item _PROP _VAR . " + filter 
				+ " }"
				;
		
		String expected = 
			prefix_p + (select.contains("skos:") ? prefix_skos : "") 
			+ select.replaceAll("_VAR", var).replaceAll("_PROP", prop )
			;
		assertSameSelect( expected, sq	);
	}		
	
	@Test public void testSingleEqualityFilterWithUnfilteredAspect() {		
		Problems p = new Problems();
		Aspect sn = new Aspect( pm, "pre:X" );
		Constraint f = new Filter(sn, Range.EQ(Term.number(17)));
		List<Constraint> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//
		String sq = q.toSparql(p, dsXY);
		Asserts.assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, " ?item pre:X ?pre_X ."
			, " ?item pre:Y ?pre_Y ."
			, " FILTER(?pre_X = 17)"
			, "}"
			);
		Asserts.assertSameSelect( expected, sq );
	}			

	final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
		.add(Setup.localAspect)
		;
	
	@Test public void testLocalSearch() {	
		String incoming = "{'pre:local' : { '@search': 'look for me'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
	//
		String sq = q.toSparql(p, dsLOC);
		assertNoProblems("translation failed", p);
		
		String expected = BunchLib.join
			(  "PREFIX text: <http://jena.apache.org/text#>"
			, "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_local"
			, "WHERE {"
			, " ?pre_local text:query 'look for me' ."
			, " ?item pre:local ?pre_local ."
			, "}"
			);
		
		assertSameSelect( expected, sq );
	}		
	
	@Test public void testGlobalSearch() {		
		Problems p = new Problems();
		SearchSpec s = new SearchSpec(Aspect.NONE, "look for me");
		ArrayList<Constraint> noFilters = new ArrayList<Constraint>();
		DataQuery q = new DataQuery
			( false
			, Constraint.filters(noFilters, BunchLib.list(s) )
			, new ArrayList<Guard>()
			, Modifiers.trivial()
			);
	//
		String sq = q.toSparql(p, dsXY);
		assertNoProblems("translation failed", p);
		
		String expected = BunchLib.join
			( "PREFIX text: <http://jena.apache.org/text#>"
			, "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y"
			, "WHERE {"
			, " ?item text:query 'look for me'."
			, " ?item pre:X ?pre_X ."
			, " ?item pre:Y ?pre_Y ."
			, "}"
			);
		
		assertSameSelect( expected, sq );
	}		
	
	@Test public void testGlobalSearchWithProperty() {		
		Problems p = new Problems();
		Shortname someProperty = X.getName();
		SearchSpec s = new SearchSpec(Aspect.NONE, "look for me", someProperty );
		ArrayList<Constraint> noFilters = new ArrayList<Constraint>();
		DataQuery q = new DataQuery
			( false
			, Constraint.filters( noFilters, BunchLib.list(s))
			, new ArrayList<Guard>()
			, Modifiers.trivial()
			);
	//
		String sq = q.toSparql(p, dsXY);
		
		Asserts.assertNoProblems("translation failed", p);
		
		String expected = BunchLib.join
			( "PREFIX text: <http://jena.apache.org/text#>"
			, "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y"
			, "WHERE {"
			, " ?item text:query (pre:X 'look for me') ."
			, " ?item pre:X ?pre_X ."
			, " ?item pre:Y ?pre_Y ."
			, "}"
			);
		
		Asserts.assertSameSelect( expected, sq );
	}		
	
	@Test public void testSingleEqualityFilterWithOptionalAspect() {
		Problems p = new Problems();
		Aspect sn = new Aspect( pm, "pre:X" );
		Constraint f = new Filter(sn, Range.EQ(Term.number(17)));
		List<Constraint> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//
		final API_Dataset dsXYopt = new API_Dataset(Setup.pseudoRoot(), null).add(X).add(Yopt);
		String sq = q.toSparql(p, dsXYopt);
		Asserts.assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, QueryTestSupport.BLOCK( 
				"?item pre:X ?pre_X ." , " FILTER(?pre_X = 17)"
			)
			, " OPTIONAL {?item pre:Y ?pre_Y .}"
			, "}"
			);
		Asserts.assertSameSelect( expected, sq );
	}		

	@Test public void testLengthCopied() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/prefixPart/").lock();
		Aspect snA = new Aspect( pm, "pre:X" );
		Aspect snB = new Aspect( pm, "pre:Y" );
		Constraint fA = new Filter(snA, Range.EQ(Term.number(8)));
		Constraint fB = new Filter(snB, Range.EQ(Term.number(9)));
		List<Constraint> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(Constraint.filters(filters), new ArrayList<Sort>(), Slice.create(17));
	//
		String sq = q.toSparql(p, dsXY);
		assertNoProblems("translation failed", p);
		String expect = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, "{ SELECT ?item ?pre_X ?pre_Y WHERE {"
			, " ?item pre:X ?pre_X ."
			, " ?item pre:Y ?pre_Y ."
			, " FILTER(?pre_X = 8)"
			, " FILTER(?pre_Y = 9)"
			, "}"
			, "LIMIT 17"
			, "}}"
			);
		assertSameSelect(expect, sq );
	}
	
	@Test public void testOffsetCopied() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/prefixPart/").lock();
		Aspect snA = new Aspect( pm, "pre:X" );
		Aspect snB = new Aspect( pm, "pre:Y" );
		Constraint fA = new Filter(snA, Range.EQ(Term.number(8)));
		Constraint fB = new Filter(snB, Range.EQ(Term.number(9)));
		List<Constraint> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(Constraint.filters(filters), new ArrayList<Sort>(), Slice.create(null, 1066));
	//
		String sq = q.toSparql(p, dsXY);
		assertNoProblems("translation failed", p);
		String expect = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, "{ SELECT ?item ?pre_X ?pre_Y WHERE {"
			, " ?item pre:X ?pre_X ."
			, " ?item pre:Y ?pre_Y ."
			, " FILTER(?pre_X = 8)"
			, " FILTER(?pre_Y = 9)"
			, "}"
			, "OFFSET 1066"
			, "}}"
			);
		assertSameSelect(expect, sq );
	}
	
	@Test public void testLengthAndOffsetCopied() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/prefixPart/").lock();
		Aspect snA = new Aspect( pm, "pre:X" );
		Aspect snB = new Aspect( pm, "pre:Y" );
		Constraint fA = new Filter(snA, Range.EQ(Term.number(8)));
		Constraint fB = new Filter(snB, Range.EQ(Term.number(9)));
		List<Constraint> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(Constraint.filters(filters), new ArrayList<Sort>(), Slice.create(17, 1829));
	//
		String sq = q.toSparql(p, dsXY);
		assertNoProblems("translation failed", p);
		String expect = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE {"
			, "{ SELECT ?item ?pre_X ?pre_Y WHERE {"
			, " ?item pre:X ?pre_X ."
			, " ?item pre:Y ?pre_Y ."
			, " FILTER(?pre_X = 8)"
			, " FILTER(?pre_Y = 9)"
			, "}"
			, "LIMIT 17 OFFSET 1829"
			, "}}"
			);
		assertSameSelect(expect, sq );
	}	
	
	@Test public void testDoubleEqualityFilter() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/prefixPart/").lock();
		Aspect snA = new Aspect(pm, "pre:X");
		Aspect snB = new Aspect(pm, "pre:Y" );
		Constraint fA = new Filter(snA, Range.EQ(Term.number(8)));
		Constraint fB = new Filter(snB, Range.EQ(Term.number(9)));
		List<Constraint> filters = BunchLib.list(fA, fB);
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//
		String sq = q.toSparql(p, dsXY);
		Asserts.assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X ?pre_Y WHERE { "
			, " ?item pre:X ?pre_X ."
			, " ?item pre:Y ?pre_Y ."
			, " FILTER(?pre_X = 8)"
			, " FILTER(?pre_Y = 9)"
			, "}"
			);
		Asserts.assertSameSelect( expected, sq );
	}
	
	@Test public void testDatasetRestriction() {
		Problems p = new Problems();
		DataQuery q = new DataQuery(Constraint.filters(BunchLib.<Constraint>list()));
	//
		API_Dataset dsBased = new API_Dataset(Setup.pseudoRoot(), null)
			.setBaseQuery("?item pre:has pre:value")
			;
	//	
		String sq = q.toSparql(p, dsBased);
		assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item WHERE {"
			, "  ?item pre:has pre:value . "
			, "}"
			);
		assertSameSelect( expected, sq );
		}
	
	@Test public void testDatasetRestrictionWithAspects() {
		Problems p = new Problems();
		DataQuery q = new DataQuery(Constraint.filters(BunchLib.<Constraint>list()));
	//		
		API_Dataset dsBased = new API_Dataset(Setup.pseudoRoot(), null)
			.add(Y)
			.setBaseQuery("?item pre:has pre:value.")
			;
	//
		String sq = q.toSparql(p, dsBased);
		assertNoProblems("translation failed", p);
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_Y WHERE {"
			, " ?item pre:has pre:value ."
			, " ?item pre:Y ?pre_Y ."
			, "}"
			);
		assertSameSelect( expected, sq );
		}
}
