/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Shortname;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestShortname {

	@Test public void testConstructShortname() {
		PrefixMapping pm = PrefixMapping.Factory.create();
		pm.setNsPrefix( "pre", "eh:/prefixPart/" );
	//
		Shortname s = new Shortname(pm, "pre:post");
		assertEquals( "eh:/prefixPart/post", s.getURI() );
		assertEquals( "pre:post", s.getCURIE() );
	}
	
	@Test public void testAsVar() {
		PrefixMapping pm = PrefixMapping.Factory.create();
		pm.setNsPrefix( "pre", "eh:/prefixPart/" );
	//
		assertEquals( "pre_post", new Shortname(pm, "pre:post").asVar());
		assertEquals( "pre_po_st", new Shortname(pm, "pre:po_st").asVar());
		assertEquals( "pre_post17", new Shortname(pm, "pre:post17").asVar());
		assertEquals( "pre_po_st", new Shortname(pm, "pre:po:st").asVar());
	//
		assertEquals( "pre_po2Dst", new Shortname(pm, "pre:po-st").asVar());
		assertEquals( "pre_po2Est", new Shortname(pm, "pre:po.st").asVar());
		assertEquals( "pre_po24st", new Shortname(pm, "pre:po$st").asVar());
		assertEquals( "pre_po2Ast", new Shortname(pm, "pre:po*st").asVar());
		assertEquals( "pre_po2Bst", new Shortname(pm, "pre:po+st").asVar());
		assertEquals( "pre_po3Dst", new Shortname(pm, "pre:po=st").asVar());
	}
}
