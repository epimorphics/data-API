/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects.tests;

import org.junit.Test;

import static org.junit.Assert.*;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Substitution;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.data_queries.terms.TermResource;
import com.epimorphics.data_api.libs.BunchLib;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestSubstitution {
	
	@Test public void testEqResourceSubstitution() {
		Aspect a = aspect("eh:/A");
		TermResource tr = new TermResource("eh:/X");
		verify(a, Operator.EQ, tr, true, tr);
		verify(a, Operator.NE, tr, false, null);
		verify(a, Operator.LE, tr, false, null);
		verify(a, Operator.LT, tr, false, null);
		verify(a, Operator.GE, tr, false, null);
		verify(a, Operator.GT, tr, false, null);
	}
	
	static final Model m = ModelFactory.createDefaultModel();
	
	private Aspect aspect(String string) {
		return new Aspect(m.createResource(string));
	}

	void verify(Aspect a, Operator op, Term provided, boolean canReplace, Term value) {
		Filter f = new Filter(a, new Range(op, BunchLib.list(provided)));
		Substitution est = new Substitution(f);
		assertSame(a, est.aspect);
		assertEquals(canReplace, est.canReplace);
		assertEquals(value, est.value);
	}

}
