/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions.tests;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.apache.jena.atlas.json.JsonBoolean;
import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Test;

import com.epimorphics.data_api.conversions.Convert;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

public class TestConversionsToJson {

	@Test public void testTranslateResource() {
		Node resource = NodeFactory.createURI("eh:/example");
		JsonObject expected = Convert.objectWith("@id", "eh:/example");
		assertEquals(expected, Convert.toJson(resource));
	}
	
	@Test public void testTranslatePlainString() {
		Node string = NodeFactory.createLiteral("spelling", "", null);
		JsonObject expected = Convert.objectWith("@value", "spelling");
		assertEquals(expected, Convert.toJson(string));
	}
	
	@Test public void testTranslateLanguagedString() {
		Node languaged = NodeFactory.createLiteral("spelling", "en-uk", null);
		JsonObject expected = Convert.objectWith("@value", "spelling", "@language", "en-uk");
		assertEquals(expected, Convert.toJson(languaged));
	}
	
	@Test public void testTranslateBooleanLiteral() {
		testTranslateBooleanLiteral(true);
		testTranslateBooleanLiteral(false);
	}

	private void testTranslateBooleanLiteral(boolean value) {
		LiteralLabel ll = LiteralLabelFactory.create(value);
		Node resource = NodeFactory.createLiteral(ll);
		JsonValue expected = new JsonBoolean(value);
		assertEquals(expected, Convert.toJson(resource));
	}
	
	@Test public void testTranslateInteger() {
		LiteralLabel ll = LiteralLabelFactory.create(17);
		Node integer = NodeFactory.createLiteral(ll);
		assertEquals(JsonNumber.valueInteger("17"), Convert.toJson(integer));		
	}
	
	@Test public void testTranslateDecimal() {
		LiteralLabel ll = LiteralLabelFactory.create(new BigDecimal("1.7"));
		Node integer = NodeFactory.createLiteral(ll);
		assertEquals(JsonNumber.valueInteger("1.7"), Convert.toJson(integer));		
	}
	
	@Test public void testTranslateDouble() {
		LiteralLabel ll = LiteralLabelFactory.create(17.0);
		Node integer = NodeFactory.createLiteral(ll);
		assertEquals(JsonNumber.valueInteger("17.0"), Convert.toJson(integer));		
	}
	
	@Test public void testTypedLiteral() {
		String URI = "eh:/types/something#";
		RDFDatatype dt = new BaseDatatype(URI);
		LiteralLabel ll = LiteralLabelFactory.create("lex", "", dt);
		Node x = NodeFactory.createLiteral(ll);
		assertEquals(Convert.objectWith("@value", "lex", "@type", URI), Convert.toJson(x));		
	}
}
