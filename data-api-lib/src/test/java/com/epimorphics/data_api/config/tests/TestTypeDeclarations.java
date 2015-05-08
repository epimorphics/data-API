/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.config.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.test_support.LoadModel;
import com.hp.hpl.jena.rdf.model.Model;
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
		API_Dataset ds = configure("<eh:/root> dsapi:literalType <eh:/LT1>, <eh:/LT2>.");
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
		Model m = LoadModel.modelFromTurtle(ttl);
		return m.createResource("eh:/root"); 
	}
}
