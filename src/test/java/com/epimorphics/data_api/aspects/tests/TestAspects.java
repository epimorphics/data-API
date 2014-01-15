/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Shortname;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestAspects {

	/*

    label -- a set (encoded as array) of strings or @value/@language objects of labels for this aspect.

    description -- a set (encoded as array) of strings or @value/@language objects of descriptions for this aspect.

    rangeType -- the URI of the type of values delivered by the aspect. 

    isOptional -- true iff this aspect is an attribute that need not be present on every element.

    isMultiValued -- true iff this aspect is an attribute that may appear multiple times for a single element.

    range -- range constraints on values of this aspect, as described below.

	*/
	
	static final PrefixMapping pm = PrefixMapping.Extended;
		
	@Test public void testAspectMembers() {
		Shortname sn = new Shortname(pm, "pre:local");
		Aspect a = new Aspect
			( "http://full-uri.org/local" 
			, sn
			);
	//
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals(sn, a.getName());
		
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
	}
	
	public static class MockAspect extends Aspect {
	
		public MockAspect(String ID) {
			super(ID, new Shortname(pm, "pre:" + tail(ID)));
		}

		private static String tail(String uri) {
			int hash = uri.lastIndexOf("#");
			int slash = uri.lastIndexOf("/");
			int cut = hash > slash ? hash : slash;
			return uri.substring(cut + 1);
		}
	}
}
