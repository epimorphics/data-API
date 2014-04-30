/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Constraint;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.NegatedOptionalAspect;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestNegation {

	static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("spoo", "eh:/namespace/")
		.lock()
		;
	
	@Test public void testNegatePlainFilters() {
		testNegateFilter(Operator.GE, Term.integer("17"), Operator.LT, Term.integer("17"));
		testNegateFilter(Operator.GT, Term.integer("17"), Operator.LE, Term.integer("17"));
		testNegateFilter(Operator.LE, Term.integer("17"), Operator.GT, Term.integer("17"));
		testNegateFilter(Operator.LT, Term.integer("17"), Operator.GE, Term.integer("17"));
		testNegateFilter(Operator.EQ, Term.integer("17"), Operator.NE, Term.integer("17"));
		testNegateFilter(Operator.NE, Term.integer("17"), Operator.EQ, Term.integer("17"));
	}
	
	@Test public void testNegateFunctionFilters() {
		testNegateFilter(Operator.CONTAINS, Term.string("17"), Operator.NOT_CONTAINS, Term.string("17"));
		testNegateFilter(Operator.MATCHES, Term.string("17"), Operator.NOT_MATCHES, Term.string("17"));
	}

	private void testNegateFilter
		( Operator expectedOp, Term expectedValue
		, Operator givenOp, Term givenValue
		) {
		Constraint arg = aFilter("spoo:local", givenOp, givenValue);
		Constraint negated = Constraint.negate( BunchLib.list(arg) );
		Constraint expected = aFilter("spoo:local", expectedOp, expectedValue);
		assertEquals( expected, negated );
	}

	private Constraint aFilter(String name, Operator op, Term t) {
		Aspect sn = new Aspect(pm, name);
		Range r = new Range(op, BunchLib.list(t));
		Constraint f = new Filter(sn, r);
		return Constraint.filters(BunchLib.list(f));
	}
	
	@Test public void testNegateOptionalFilter() {
		Term v = Term.integer("17");
		Aspect A = new Aspect(pm, "spoo:A").setIsOptional(true);
		
		Range r = new Range(Operator.LE, BunchLib.list(v));
		Constraint f = new Filter(A, r);
		Constraint c = Constraint.filters(BunchLib.list(f));
		
//		System.err.println( ">> c = " + c );
		
		Range notR = new Range(Operator.GT, BunchLib.list(v));
		Filter notF = new Filter(A, notR );
		
		Constraint expected = new NegatedOptionalAspect(notF);
		assertEquals(expected, Constraint.negate(BunchLib.list(c)));
	}
	
	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
		.add(new Aspect(pm, "spoo:local").setIsOptional(true))
		;
	
	static { ds.getPrefixes().setNsPrefixes(pm); }
	
	@Test public void testGenerateNegatedOptionalInfixQuery() {
		testGeneratedNegatedOptional("@lt", ">=");
		testGeneratedNegatedOptional("@le", ">");
		testGeneratedNegatedOptional("@gt", "<=");
		testGeneratedNegatedOptional("@ge", "<");
		testGeneratedNegatedOptional("@eq", "!=");
		testGeneratedNegatedOptional("@ne", "=");
	}
	
	@Test public void testGenerateNegatedOptionalFunction() {
//		testGeneratedNegatedOptionalFunction("@contains", "!CONTAINS");
		testGeneratedNegatedOptionalFunction("@matches", "!REGEX");
	}
	
	private void testGeneratedNegatedOptionalFunction(String op, String negFun) {
		
		String incoming = "{'@not': [{'spoo:local': {'_OP': 'target'}}]}".replaceAll("_OP", op);
		JsonObject jo = JSON.parse(incoming);
		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		
		Asserts.assertNoProblems("did not generate valid SPARQL", p);
		
		String s = q.toSparql(p, ds);
		
//		System.err.println( ">> generated:\n" + s );
		
		String expected = BunchLib.join
			( "PREFIX spoo: <eh:/namespace/>"
			, "SELECT ?item ?spoo_local"
			, "WHERE {"
			, "  OPTIONAL { ?item spoo:local ?spoo_local }"
			, "  FILTER( _NEGFUN(?spoo_local, 'target') || !bound(?spoo_local))".replace("_NEGFUN", negFun)
			, "}"
			);
		
		Asserts.assertSameSelect(expected, s);
	}

	private void testGeneratedNegatedOptional(String op, String negOp) {
		
		String incoming = "{'@not': [{'spoo:local': {'_OP': 17}}]}".replaceAll("_OP", op);
		JsonObject jo = JSON.parse(incoming);
		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		
		Asserts.assertNoProblems("did not generate valid SPARQL", p);
		
		String s = q.toSparql(p, ds);
		
//		System.err.println( ">> generated:\n" + s );
		
		String expected = BunchLib.join
			( "PREFIX spoo: <eh:/namespace/>"
			, "SELECT ?item ?spoo_local"
			, "WHERE {"
			, "  OPTIONAL { ?item spoo:local ?spoo_local }"
			, "  FILTER(?spoo_local _NEGOP 17 || !bound(?spoo_local))".replace("_NEGOP", negOp)
			, "}"
			);
		
		Asserts.assertSameSelect(expected, s);
	}
	
}
