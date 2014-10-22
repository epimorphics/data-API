/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.config.tests;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;

import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.vocabs.Dsapi;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

public class TestTypeDeclarations {
	
	@Test public void testUndeclaredDatatypes() {
		API_Dataset ds = configure("");
		Model m = ds.getRoot().getModel();
		assertFalse(ds.isLiteralType(m.createResource("eh:/notLiteral")));
	}
	
	@Test public void testSetLiteralTYpe() {
		API_Dataset ds = configure("");
		Model m = ds.getRoot().getModel();
		Resource type = m.createResource("eh:/literally");
		assertFalse(ds.isLiteralType(type));
		ds.setIsLiteralType(type);
		assertTrue(ds.isLiteralType(type));
	}

	@Test public void testBuiltinDatatypes() {
		API_Dataset ds = configure("");
		Model m = ds.getRoot().getModel();
		assertTrue(ds.isLiteralType(xsd(m, "boolean")));
	}
	
	@Test public void testDeclarations() {
		API_Dataset ds = configure("");
		Model m = ds.getRoot().getModel();
		Resource type = m.createResource(m.expandPrefix("rdf:type"));
		assertFalse(ds.isLiteralType(type));
	}
	
	@Test public void testDeclareTypes() {
//		API_Dataset ds = configure("<eh:/root> dsapi:literalType <eh:/LT1>, <eh:/LT2>.");
		API_Dataset ds = configure
			( "<eh:/root> dsapi:aspect <eh:/A1>, <eh:/A2>"
			+ ". <eh:/A1> dsapi:property <eh:/P1>"
			+ ". <eh:/A2> dsapi:property <eh:/P2>"
			+ ". <eh:/P1> rdfs:range <eh:/LT1>"
			+ ". <eh:/P2> rdfs:range <eh:/LT2>"
			+ "."
			);
		Model m = ds.getRoot().getModel();
		assertTrue(ds.isLiteralType(m.createResource("eh:/LT1")));
		assertTrue(ds.isLiteralType(m.createResource("eh:/LT2")));
		assertFalse(ds.isLiteralType(m.createResource("eh:/OT1")));
		assertFalse(ds.isLiteralType(m.createResource("eh:/OT2")));
	}

	private Resource xsd(Model m, String localName) {
		return m.createResource(XSD.getURI() + localName);
	}

	private API_Dataset configure(String configString) {
		Resource config = makeConfig(configString);
		return new API_Dataset(config, new DSAPIManager());
	}

	private Resource makeConfig(String ttl) {
		Model m = modelFromTurtle(ttl);
		Resource root = m.createResource("eh:/root");
		return root; 
	}	
	
	/**
    	The prefixes used by default by modelFromTurtle.
	*/
	public static final String PREFIXES = 
	    "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n"
	    + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
	    + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
	    + "@prefix xsd: <" + XSD.getURI() + "> .\n"
	    + "@prefix : <http://www.epimorphics.com/tools/example#> .\n"
	    + "@prefix dsapi: <" + Dsapi.NS + "> ."
	    ;
	
	/**
	    Create a model by reading the Turtle string ttl, using the prefixes
	    from PREFIXES.
	*/
	public static Model modelFromTurtle(String ttl) {
		Model model = ModelFactory.createDefaultModel();
		model.read( new StringReader(PREFIXES + ttl), null, "Turtle");
		return model;
	}
}
