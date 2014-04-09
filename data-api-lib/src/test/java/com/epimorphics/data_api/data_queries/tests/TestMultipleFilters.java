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
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestMultipleFilters {

	static final API_Dataset ds = 
		new API_Dataset(Setup.pseudoRoot(), null)
			.add(Setup.localAspect)
			;
	
	@Test public void testDoubleFilter() {
		PrefixMapping pm = ds.getPrefixes();
		Aspect local = new Aspect(pm, "pre:local");
		Problems p = new Problems();
	//		
		Constraint lt = new Filter(local, new Range(Operator.LT, BunchLib.list(Term.decimal("17"))));
		Constraint gt = new Filter(local, new Range(Operator.GT, BunchLib.list(Term.decimal("42"))));
	//
		List<Constraint> filters = BunchLib.list(lt, gt);
		
		DataQuery dq = new DataQuery(Constraint.filters(filters));

		API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
			.add(Setup.localAspect)
			;
		
		String sq = dq.toSparql(p, ds);
		Asserts.assertNoProblems("filter translation failed", p);
		
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_local"
			, "WHERE {"
			, " ?item pre:local ?pre_local"
			, " FILTER(?pre_local < 17)"
			, " FILTER(?pre_local > 42)"
			, "}"
			);
		
		Asserts.assertSameSelect(expected, sq);			
		}
}

