/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Composition;
import com.epimorphics.data_api.data_queries.Filter;
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
		testNegateFilter("ge", Term.integer("17"), "lt", Term.integer("17"));
		testNegateFilter("gt", Term.integer("17"), "le", Term.integer("17"));
		testNegateFilter("le", Term.integer("17"), "gt", Term.integer("17"));
		testNegateFilter("lt", Term.integer("17"), "ge", Term.integer("17"));
		testNegateFilter("eq", Term.integer("17"), "ne", Term.integer("17"));
		testNegateFilter("ne", Term.integer("17"), "eq", Term.integer("17"));
	}

	private void testNegateFilter
		( String expectedOp, Term expectedValue
		, String givenOp, Term givenValue
		) {
		Composition arg = aFilter("spoo:local", givenOp, givenValue);
		Composition negated = Composition.negate( BunchLib.list(arg) );
		Composition expected = aFilter("spoo:local", expectedOp, expectedValue);
		
//		System.err.println( ">> expected: " + expected );
//		System.err.println( ">> negated:  " + negated );
		
		assertEquals( expected, negated );
	}

	private Composition aFilter(String name, String op, Term t) {
		Shortname sn = new Shortname(pm, name);
		Range r = new Range(op, BunchLib.list(t));
		Filter f = new Filter(sn, r);
		return Composition.filters(BunchLib.list(f));
	}
}
