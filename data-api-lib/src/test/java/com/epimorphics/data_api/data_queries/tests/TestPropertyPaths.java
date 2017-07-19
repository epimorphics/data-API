/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import java.util.List;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Constraint;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.parse_data_query.tests.Setup;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;
import org.apache.jena.shared.PrefixMapping;

public class TestPropertyPaths {

	static final PrefixMapping pm = Setup.pm;
	
	static final Aspect X = new Aspect(pm, "pre:X" )
		.setPropertyPath("pre:A/pre:B")
		;

	@Test public void testQueryExpandsAspectPropertyPath() {
		Problems p = new Problems();
		Aspect sn = new Aspect( pm, "pre:X" );
		Constraint f = new Filter(sn, new Range(Operator.GT, BunchLib.list(Term.number(17))));
		List<Constraint> filters = BunchLib.list(f);
		DataQuery q = new DataQuery(Constraint.filters(filters));
	//
		final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
			.add(X)
			;
	//
		String sq = q.toSparql(p, ds);
		
		Asserts.assertNoProblems("translation failed", p);	
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_X WHERE"
			, "{"
			, " ?item pre:A ?pre_A ."
			, " ?pre_A pre:B ?pre_X ."
			, "FILTER(?pre_X " + ">" + " 17)"
			, "}"
			);
		Asserts.assertSameSelect( expected, sq );
	}
	
	
}
