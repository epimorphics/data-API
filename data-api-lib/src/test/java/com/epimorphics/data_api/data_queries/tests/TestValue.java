/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Value;

public class TestValue {
	
	@Test public void testValue() {
		assertEquals(Value.wrap(17), Value.wrap(17));
		assertEquals(Value.wrap("17"), Value.wrap("17"));
		assertEquals(Value.wrap(true), Value.wrap(true));
	//
		assertEquals(new Integer(17).hashCode(), Value.wrap(17).hashCode() );
		assertEquals("hedgehog".hashCode(), Value.wrap("hedgehog").hashCode() );
	//
		assertEquals("answer", Value.wrap("answer").unwrap());
		assertEquals(66, Value.wrap(66).unwrap());
	}
}
