/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Constraint;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.terms.Term;
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
		Constraint arg = aFilter("spoo:local", givenOp, givenValue);
		Constraint negated = Constraint.negate( BunchLib.list(arg) );
		Constraint expected = aFilter("spoo:local", expectedOp, expectedValue);
		assertEquals( expected, negated );
	}

	private Constraint aFilter(String name, Operator op, Term t) {
		Aspect sn = new Aspect(pm, name);
		Range r = new Range(op, BunchLib.list(t));
		Filter f = new Filter(sn, r);
		return Constraint.filters(BunchLib.list(f));
	}
	
	
//	@Test public void testNegateOptionalFilter() {
//		Term v = Term.integer("17");
//		Aspect A = new Aspect(pm, "spoo:A").setIsOptional(true);
//		
//		Range r = new Range(Operator.LE, BunchLib.list(v));
//		Filter f = new Filter(A, r);
//		Composition c = Composition.filters(BunchLib.list(f));
//		
//		System.err.println( ">> c = " + c );
//		
//		Range notR = new Range(Operator.GT, BunchLib.list(v));
//		Filter notF = new Filter(A, notR );
//		
//		Filter unboundA = new UnboundFilter(A);
//		
//		Composition expected = Composition.smallOr( notF, unboundA );
//		
//		assertEquals(expected, Composition.negate(BunchLib.list(c)));
//		
//	}
	
	
	
}
