/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Shortname;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestShortname {

	@Test public void testConstructShortname() {
		PrefixMapping pm = PrefixMapping.Factory.create();
		pm.setNsPrefix( "pre", "eh:/prefixPart/" );
	//
		Shortname s = new Shortname(pm, "pre:post");
		assertEquals( "eh:/prefixPart/post", s.getURI() );
		assertEquals( "pre:post", s.getCURIE() );
	}
	
}
