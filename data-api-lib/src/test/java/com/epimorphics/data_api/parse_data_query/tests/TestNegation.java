/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Composition;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestNegation {

	static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("spoo", "eh:/namespace/")
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
		Composition arg = aFilter("spoo:local", givenOp, givenValue);
		Composition negated = Composition.negate( BunchLib.list(arg) );
		Composition expected = aFilter("spoo:local", expectedOp, expectedValue);
		
//		System.err.println( ">> expected: " + expected );
//		System.err.println( ">> negated:  " + negated );
		
		assertEquals( expected, negated );
	}

	private Composition aFilter(String name, Operator op, Term t) {
		Shortname sn = new Shortname(pm, name);
		Range r = new Range(op, BunchLib.list(t));
		Filter f = new Filter(sn, r);
		return Composition.filters(BunchLib.list(f));
	}
}
