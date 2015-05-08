/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.parse_data_query.tests.Setup;
import com.epimorphics.data_api.test_support.LoadModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestAspects {
	
	static final PrefixMapping pm = Setup.pm;
		
	@Test public void testAspectMembers() {
		Shortname sn = new Shortname(pm, "pre:local");
		Aspect a = new Aspect(pm, "pre:local" );
	//
		assertEquals("eh:/prefixPart/local", a.getID());
		assertEquals(sn, a.getName());
	}
	
	@Test public void testAspectBunch() {
		Aspect A = new MockAspect("eh:/mock/aspect/A");
		Aspect B = new MockAspect("eh:/mock/aspect/B");
		Aspects a = new Aspects();
		assertEquals(0, a.getAspects().size());
		assertSame(a, a.include(A));
		assertEquals(BunchLib.set(A), a.getAspects());
		assertSame(a, a.include(B));
		assertEquals(BunchLib.set(A, B), a.getAspects());
	}
	
	/*
     * Tests removed as part of refactoring Aspect implementation
     * 
	@Test public void testAspectLabel() {
		Aspect a = new MockAspect("eh:/mock/aspect/A");
		assertEquals(BunchLib.list(), a.getLabels());
		List<Node> stringies = BunchLib.list(stringNode("paddington"), stringNode("marylebone"));
		assertSame(a, a.setLabels(stringies));
		assertEquals(stringies, a.getLabels());
	}
	
	Node stringNode(String s) {
		return NodeFactory.createLiteral(s, "", null);
	}
	
	@Test public void testAspectDescription() {
		Aspect a = new MockAspect("eh:/mock/aspect/B");
		assertEquals(BunchLib.list(), a.getDescriptions());
		List<Node> stringies = BunchLib.list
			( stringNode("a very long way from anywhere")
			, stringNode("a maze of twisty little passages, all alike"
			));
		assertSame(a, a.setDescriptions(stringies));
		assertEquals(stringies, a.getDescriptions());
	}
     */
	
	@Test public void testAspectDefaultRangeType() {
		Aspect a = new MockAspect("eh:/mock/aspect/C");
		assertNull(a.getRangeType());
	}
	
	@Test public void testAspectRangeType() {
		Resource type = RDFS.Class;
		Aspect a = new MockAspect("eh:/mock/aspect/C");
		assertNull(a.getRangeType());
		assertSame(a, a.setRangeType(type));
		assertEquals(type, a.getRangeType());
	}
	
	@Test public void testAspectConfigureRangeType() {
		Model m = LoadModel.modelFromTurtle("<eh:/root> rdfs:range <eh:/T>");
		Resource root = m.createResource("eh:/root");
		Resource type = m.createResource("eh:/T");
		Aspect a = new Aspect(root);
		assertEquals(type, a.getRangeType());
	}
	
	@Test public void testAspectIsOptional() {
		Aspect a = new MockAspect("eh:/mock/aspect/D");
		assertEquals(false, a.getIsOptional());
		assertSame(a, a.setIsOptional(true));
		assertEquals(true, a.getIsOptional());
	}
	
	@Test public void testAspectIsMultiValued() {
		Aspect a = new MockAspect("eh:/mock/aspect/E");;
		assertEquals(false, a.getIsMultiValued());
		assertSame(a, a.setIsMultiValued(true));
		assertEquals(true, a.getIsMultiValued());
	}
	
	@Test public void testAspectDefaultPropertyPaths() {
		Aspect a = new MockAspect("eh:/mock/aspect/X" );
		assertEquals( "pre:X", a.getName().getCURIE());
		assertEquals( "pre:X", a.getPropertyPath());
		assertNull(a.getPropertyPathRaw());
	}
	
	@Test public void testAspectDefinedPropertyPaths() {
		Aspect a = new MockAspect("eh:/mock/aspect/X" );
		
		a.setPropertyPath("pre:A/pre:B");
		
		assertEquals( "pre:X", a.getName().getCURIE());
		assertEquals( "pre:A/pre:B", a.getPropertyPath());
		assertEquals( "pre:A/pre:B", a.getPropertyPathRaw());
	}
	
	public static class MockAspect extends Aspect {
	
		public MockAspect(String ID) {
			super(pm, "pre:" + tail(ID));
		}

		private static String tail(String uri) {
			int hash = uri.lastIndexOf("#");
			int slash = uri.lastIndexOf("/");
			int cut = hash > slash ? hash : slash;
			return uri.substring(cut + 1);
		}
	}
}
