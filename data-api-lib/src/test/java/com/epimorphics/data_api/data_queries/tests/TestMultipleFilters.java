/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.Composition;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Shortname;
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
		Shortname local = new Shortname(pm, "pre:local");
		Problems p = new Problems();
	//		
		Filter lt = new Filter(local, new Range(Operator.LT, BunchLib.list(Term.decimal("17"))));
		Filter gt = new Filter(local, new Range(Operator.GT, BunchLib.list(Term.decimal("42"))));
	//
		List<Filter> filters = BunchLib.list(lt, gt);
		
		DataQuery dq = new DataQuery(Composition.filters(filters));
		
		Aspects a = new Aspects().include(Setup.localAspect);
		
		String sq = dq.toSparql(p, a, null, pm);
		Asserts.assertNoProblems("filter translation failed", p);
		
		String expected = BunchLib.join
			( "PREFIX pre: <eh:/prefixPart/>"
			, "SELECT ?item ?pre_local"
			, "WHERE {"
			, " ?item pre:local ?pre_local"
			, "FILTER((?pre_local < 17) && (?pre_local > 42))"
//			 , " FILTER (?pre_local < 17) FILTER(?pre_local > 42)"
			, "}"
			);
		
		Asserts.assertSameSelect(expected, sq);			
		}
}

