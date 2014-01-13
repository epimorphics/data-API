/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Value;

public class TestFilter {
	
	@Test public void testFilterEquality() {
		Range r = Range.EQ(Value.wrap(0));
		Filter f = new Filter( "name", r );
		assertEquals( f, new Filter( "name", r ) );
		assertDiffer( f, new Filter( "name", Range.EQ(Value.wrap(1))));
		assertDiffer( f, new Filter( "anon", Range.EQ(Value.wrap(0))));
	}
	
	@Test public void testFilterHashcode() {
		Range r0 = Range.EQ(Value.wrap(0));
		Range r1 = Range.EQ(Value.wrap(1));
	//
		assertEquals("name".hashCode() ^ r0.hashCode(), new Filter("name", r0).hashCode());
		assertEquals("anon".hashCode() ^ r0.hashCode(), new Filter("anon", r0).hashCode());
		assertEquals("name".hashCode() ^ r1.hashCode(), new Filter("name", r1).hashCode());
	}
	
	static void assertDiffer(Object expect, Object o) {
		if (expect.equals(o)) fail("expected something different from " + expect);
	}
	
	

}
