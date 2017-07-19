/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.search.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.SearchSpec;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.parse_data_query.tests.Setup;
import org.apache.jena.shared.PrefixMapping;

public class TestCombinedSearch {

	static final API_Dataset ds = 
		new API_Dataset(Setup.pseudoRoot(), null)
			.add(Setup.localAspect)
			;
	
	static final PrefixMapping pm = ds.getPrefixes();
	
	static final Aspect alpha = new Aspect(pm, "pre:alpha");
	static final Aspect delta = new Aspect(pm, "pre:delta");
	
	@Test public void testWithExplicitFieldFromImplicitProperty() {
		SearchSpec s = new SearchSpec(alpha, "word" );
		SearchSpec e = s.withExplicitField();
		assertEquals("alpha: word", e.getPattern());		
	}
	
	@Test public void testWithExplicitFieldFromExplicitProperty() {
		Shortname phrase = new Shortname(pm, "pre:phrase");
		SearchSpec s = new SearchSpec(alpha, "word", phrase );
		SearchSpec e = s.withExplicitField();
		assertEquals("phrase: word", e.getPattern());		
	}
	
	@Test public void testSearchSpecAnd() {
		SearchSpec a = new SearchSpec(alpha, "word");
		SearchSpec d = new SearchSpec(delta, "sign");
		SearchSpec x = a.withExplicitField().withAndExplicitField(d);
		assertEquals("alpha: word AND delta: sign", x.getPattern());		
	}
	
	
	
}
