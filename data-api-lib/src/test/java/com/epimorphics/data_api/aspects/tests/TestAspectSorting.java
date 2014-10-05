/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects.tests;

import static com.epimorphics.data_api.libs.BunchLib.list;
import static com.epimorphics.data_api.libs.BunchLib.set;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestAspectSorting {

	static Model m = ModelFactory.createDefaultModel();
	
	Resource r(String u) {
		return m.createResource(u);
	}
	
	@Test public void testSortingWithoutOptionals() {
		
		Aspect a = new Aspect(r("eh:/A"));
		Aspect b = new Aspect(r("eh:/B"));
		Aspect c = new Aspect(r("eh:/C"));
		Aspect d = new Aspect(r("eh:/D"));
		
		Set<Aspect> AB = set(a, b);
		
		checkSorted(list(a, c), AB, list(c, a));
		checkSorted(list(a, b), AB, list(b, a));
		checkSorted(list(a, b, c, d), AB, list(d, c, b, a));
		checkSorted(list(c, d, a, b), set(c, d), list(a, b, c, d));
	}
	
	@Test public void testSortingWithOptionals() {
		
		Aspect a = new Aspect(r("eh:/A"));
		Aspect b = new Aspect(r("eh:/B"));
		Aspect c = new Aspect(r("eh:/C")).setIsOptional(true);
		Aspect d = new Aspect(r("eh:/D")).setIsOptional(true);
		
		Set<Aspect> AB = set(a, b);
		
		checkSorted(list(a, c), AB, list(c, a));
		checkSorted(list(a, b), AB, list(b, a));
		checkSorted(list(a, b, c, d), AB, list(d, c, b, a));
		checkSorted(list(c, d, a, b), set(c, d), list(a, b, c, d));
	}
	
	@Test public void testUnconstrainedSortingWithOptionals() {
		
		Aspect a = new Aspect(r("eh:/A"));
		Aspect b = new Aspect(r("eh:/B"));
		Aspect c = new Aspect(r("eh:/C")).setIsOptional(true);
		Aspect d = new Aspect(r("eh:/D")).setIsOptional(true);
		
		Set<Aspect> none = set();
		
		checkSorted(list(a, c), none, list(c, a));
		checkSorted(list(a, b), none, list(b, a));
		checkSorted(list(a, b, c, d), none, list(d, c, b, a));
		checkSorted(list(c, d, a, b), set(c, d), list(a, b, c, d));
	}

	private void checkSorted(List<Aspect> expect, Set<Aspect> constrained,	List<Aspect> toOrder) {
		Collections.sort(toOrder, Aspect.compareConstrainedAspects(null,constrained));
		assertEquals(expect, toOrder);
	}
}
