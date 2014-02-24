/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Term;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestTerms {
	
	PrefixMapping pm = PrefixMapping.Standard;
	
	@Test public void testValue() {
		assertEquals(Term.number(17), Term.number(17));
		assertEquals(Term.string("17"), Term.string("17"));
		assertEquals(Term.bool(true), Term.bool(true));
	//
		assertEquals(new Integer(17).hashCode(), Term.number(17).hashCode() );
		assertEquals("hedgehog".hashCode(), Term.string("hedgehog").hashCode() );
	//
		assertEquals("\"answer\"", Term.string("answer").asSparqlTerm(pm));
		assertEquals("66", Term.number(66).asSparqlTerm(pm));
	//
		assertEquals(Term.typed("chat", "xsd:string"), Term.typed("chat", "xsd:string"));
		assertEquals("\"99\"^^xsd:integer", Term.typed("99", "xsd:integer").asSparqlTerm(pm));
	//	
		assertEquals(Term.languaged("chat", "en-uk"), Term.languaged("chat", "en-uk"));
		assertEquals("\"chat\"@en-uk", Term.languaged("chat", "en-uk").asSparqlTerm(pm));
	//
		assertEquals("\"with\\\"quote\"@en-us", Term.languaged("with\"quote",  "en-us").asSparqlTerm(pm));
		assertEquals("\"with\\\"quote\"", Term.string("with\"quote").asSparqlTerm(pm));
	}
}
