/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets.tests;

import static org.junit.Assert.*;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.vocabs.Dsapi;

public class TestAPI_Dataset {

	@Test public void testNamedDataset() {
		assertEquals("bobcat", new API_Dataset( "bobcat" ).getName());
		assertEquals("feline", new API_Dataset( "feline" ).getName());
	}
	
	@Test public void testDatasetAspects() {
		API_Dataset d = new API_Dataset("example");
		Aspect a = new TestAspects.MockAspect("eh:/A");
		Aspect b = new TestAspects.MockAspect("eh:/B");
		assertEquals(0, d.getAspects().size());
	//
		d.add(a);
		assertEquals(BunchLib.set(a), d.getAspects());
	//
		d.add(b);
		assertEquals(BunchLib.set(a, b), d.getAspects());
	}
	
	@Test public void testModifierVocabulary() {
		assertEquals(Dsapi.NS + "modifiers", Dsapi.modifiers.getURI());
		assertEquals(Dsapi.NS + "inner", Dsapi.inner.getURI());
		assertEquals(Dsapi.NS + "outer", Dsapi.outer.getURI());
	}
	
	@Test public void testDefaultModifierPosition() {
		Resource mp = new API_Dataset().getModifiersPosition();
		assertEquals(Dsapi.inner, mp);
	}
	
	@Test public void testExplicitModifierPosition() {
		Model config = ModelFactory.createDefaultModel();
		Resource root = config.createResource("eh:/root");
		
		Resource mpAbsent = new API_Dataset(root, null).getModifiersPosition();
		assertEquals(Dsapi.inner, mpAbsent);
		
		root.addProperty(Dsapi.modifiers, Dsapi.inner);
		Resource mpInner = new API_Dataset(root, null).getModifiersPosition();
		assertEquals(Dsapi.inner, mpInner);
		
		root.removeProperties().addProperty(Dsapi.modifiers, Dsapi.outer);
		Resource mpOuter = new API_Dataset(root, null).getModifiersPosition();
		assertEquals(Dsapi.outer, mpOuter);
	}
	
	@Test public void testTrapsIllegalPositions() {
		
		Model config = ModelFactory.createDefaultModel();
		Resource root = config.createResource("eh:/root");
		
		assertTraps(root, ResourceFactory.createResource("eh:/illegal"));
		assertTraps(root, ResourceFactory.createResource());
		assertTraps(root, ResourceFactory.createPlainLiteral("illegal"));
	}

	private void assertTraps(Resource root, RDFNode x) {
		try {
			root.removeProperties().addProperty(Dsapi.modifiers, x);
			new API_Dataset(root, null);
		} catch (IllegalArgumentException r) {
			return;
		}
		fail("should trap illegal modifier position: " + x);
	}
	
	
}
