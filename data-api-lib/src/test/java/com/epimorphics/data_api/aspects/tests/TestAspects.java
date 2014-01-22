/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.libs.BunchLib;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

public class TestAspects {
	
	static final PrefixMapping pm = PrefixMapping.Extended;
		
	@Test public void testAspectMembers() {
		Shortname sn = new Shortname(pm, "pre:local");
		Aspect a = new Aspect
			( "http://full-uri.org/local" 
			, sn
			);
	//
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals(sn, a.getName());
		
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
		assertEquals("http://full-uri.org/local", a.getID());
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
	
	@Test public void testAspectRangeType() {
		Resource type = RDFS.Class;
		Aspect a = new MockAspect("eh:/mock/aspect/C");
		assertNull(a.getRangeType());
		assertSame(a, a.setRangeType(type));
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
	
	@Test public void testAspectRange() {
		Aspect a = new MockAspect("eh:/mock/aspect/A");
		System.err.println( ">> testAspectRange TBD" );
	}
	
	public static class MockAspect extends Aspect {
	
		public MockAspect(String ID) {
			super(ID, new Shortname(pm, "pre:" + tail(ID)));
		}

		private static String tail(String uri) {
			int hash = uri.lastIndexOf("#");
			int slash = uri.lastIndexOf("/");
			int cut = hash > slash ? hash : slash;
			return uri.substring(cut + 1);
		}
	}
}
