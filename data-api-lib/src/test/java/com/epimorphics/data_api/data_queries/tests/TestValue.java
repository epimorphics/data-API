/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Term;

public class TestValue {
	
	@Test public void testValue() {
		assertEquals(Term.number(17), Term.number(17));
		assertEquals(Term.string("17"), Term.string("17"));
		assertEquals(Term.bool(true), Term.bool(true));
	//
		assertEquals(new Integer(17).hashCode(), Term.number(17).hashCode() );
		assertEquals("hedgehog".hashCode(), Term.string("hedgehog").hashCode() );
	//
		assertEquals("answer", Term.string("answer").unwrap());
		assertEquals(66, Term.number(66).unwrap());
	}
}
