/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Value;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestFilter {
	
	@Test public void testFilterEquality() {
		Range r = Range.EQ(Value.wrap(0));
		Filter f = new Filter( sn("name"), r );
		assertEquals( f, new Filter( sn("name"), r ) );
		assertDiffer( f, new Filter( sn("name"), Range.EQ(Value.wrap(1))));
		assertDiffer( f, new Filter( sn("anon"), Range.EQ(Value.wrap(0))));
	}
	
	@Test public void testFilterHashcode() {
		Range r0 = Range.EQ(Value.wrap(0));
		Range r1 = Range.EQ(Value.wrap(1));
	//
		assertEquals(sn("name").hashCode() ^ r0.hashCode(), new Filter(sn("name"), r0).hashCode());
		assertEquals(sn("anon").hashCode() ^ r0.hashCode(), new Filter(sn("anon"), r0).hashCode());
		assertEquals(sn("name").hashCode() ^ r1.hashCode(), new Filter(sn("name"), r1).hashCode());
	}
	
	static void assertDiffer(Object expect, Object o) {
		if (expect.equals(o)) fail("expected something different from " + expect);
	}
	
	static PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("pre", "eh:/prefixPart").lock();
	
	Shortname sn(String name) {
		return new Shortname(pm, "pre:" + name);
	}
	
	

}
