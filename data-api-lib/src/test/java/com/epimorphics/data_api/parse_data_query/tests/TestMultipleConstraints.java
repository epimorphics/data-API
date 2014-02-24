/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestMultipleConstraints {

	static final API_Dataset ds = 
		new API_Dataset(Setup.pseudoRoot(), null)
			.add(Setup.localAspect)
			;
	
	@Test public void testParseMultipleConstraints() {
		PrefixMapping pm = ds.getPrefixes();
		Shortname local = new Shortname(pm, "pre:local");
		String incoming = "{'pre:local': {'@lt': 17, '@gt': 42}}";
		JsonObject jo = JSON.parse(incoming);		
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
	//
		assertTrue(p.isOK());
	//		
		Filter lt = new Filter(local, new Range("lt", BunchLib.list(Term.decimal("17"))));
		Filter gt = new Filter(local, new Range("gt", BunchLib.list(Term.decimal("42"))));
	//
		List<Filter> filters = BunchLib.list(lt, gt);
		assertEquals( filters, q.filters() );
	}
}
