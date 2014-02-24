/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions.tests;

import java.math.BigDecimal;
import org.junit.Test;

import com.epimorphics.data_api.data_queries.Term;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

import static org.junit.Assert.assertEquals;

public class TestConvertNodeToTerm {

	@Test public void testTranslateResource() {
		Node resource = NodeFactory.createURI("eh:/example");
		assertEquals(Term.URI("eh:/example"), Term.fromNode(resource));
	}
	
	@Test public void testTranslatePlainString() {
		Node string = NodeFactory.createLiteral("spelling", "", null);
		assertEquals(Term.string("spelling"), Term.fromNode(string));
	}
	
	@Test public void testTranslateLanguagedString() {
		Node languaged = NodeFactory.createLiteral("spelling", "en-uk", null);
		assertEquals(Term.languaged("spelling", "en-uk"), Term.fromNode(languaged));
	}
	
	@Test public void testTranslateBooleanLiteral() {
		testTranslateBooleanLiteral(true);
		testTranslateBooleanLiteral(false);
	}

	private void testTranslateBooleanLiteral(boolean value) {
		LiteralLabel ll = LiteralLabelFactory.create(value);
		Node resource = NodeFactory.createLiteral(ll);
		assertEquals(Term.bool(value), Term.fromNode(resource));
	}
	
	@Test public void testTranslateInteger() {
		LiteralLabel ll = LiteralLabelFactory.create(17);
		Node integer = NodeFactory.createLiteral(ll);
		assertEquals(Term.integer("17"), Term.fromNode(integer));		
	}
	
	@Test public void testTranslateDecimal() {
		LiteralLabel ll = LiteralLabelFactory.create(new BigDecimal("1.7"));
		Node integer = NodeFactory.createLiteral(ll);
		assertEquals(Term.decimal("1.7"), Term.fromNode(integer));		
	}
	
	@Test public void testTranslateDouble() {
		LiteralLabel ll = LiteralLabelFactory.create(17.0);
		Node integer = NodeFactory.createLiteral(ll);
		assertEquals(Term.Double("17.0"), Term.fromNode(integer));		
	}
	
	@Test public void testTypedLiteral() {
		String URI = "eh:/types/something#";
		RDFDatatype dt = new BaseDatatype(URI);
		LiteralLabel ll = LiteralLabelFactory.create("lex", "", dt);
		Node x = NodeFactory.createLiteral(ll);
		assertEquals(Term.typed("lex", URI), Term.fromNode(x));		
	}
}