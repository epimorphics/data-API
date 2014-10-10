/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects.tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Substitution;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.data_queries.terms.TermArray;
import com.epimorphics.data_api.data_queries.terms.TermBool;
import com.epimorphics.data_api.data_queries.terms.TermLanguaged;
import com.epimorphics.data_api.data_queries.terms.TermNumber;
import com.epimorphics.data_api.data_queries.terms.TermResource;
import com.epimorphics.data_api.data_queries.terms.TermString;
import com.epimorphics.data_api.data_queries.terms.TermTyped;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestSubstitution {
	
	static final Model m = ModelFactory.createDefaultModel();
	
	private static Aspect aspect(String string) {
		return new Aspect(m.createResource(string));
	}	
	
	static final Aspect untyped = aspect("eh:/A");

	static final TermResource resourceX = new TermResource("eh:/X");

	static final TermArray emptyArray = new TermArray(new ArrayList<Term>());
	
	static final TermTyped typedAsInt = new TermTyped("17", "xsd:integer");

	@Test public void testEqResourceSubstitution() {
		verify(untyped, resourceX, true, resourceX);
	}
	
	@Test public void testEqLiteralFailsWithUntyped() {
		verify(untyped, new TermBool(true), true, new TermBool(true));
		verify(untyped, new TermBool(false), true, new TermBool(false));
		verify(untyped, new TermString("lo"), false, null);
	//
		verify(untyped, emptyArray, false, null);
		verify(untyped, typedAsInt, false, null);
	//
		verify(untyped, new TermNumber(17), false, null);
		verify(untyped, new TermLanguaged("chat","en"), false, null);
	}
	
	static final List<Operator> AllOperators = BunchLib.list
		( Operator.EQ
		, Operator.NE
		, Operator.LT
		, Operator.LE
		, Operator.GE
		, Operator.GT
		, Operator.MATCHES
		, Operator.CONTAINS
		, Operator.ONEOF
		, Operator.BELOW
		, Operator.SEARCH
		, Operator.NOT_MATCHES
		, Operator.NOT_CONTAINS
		, Operator.NOT_BELOW
		, Operator.NOT_SEARCH
		, Operator.NOT_ONEOF
		);

	/**
	    Verify that substituting using filter (A, EQ, provided) returns
	    the given canReplace value and term, and that all the other
	    operators return canReplace false and a null value. 
	*/
	void verify(Aspect a, Term provided, boolean canReplace, Term value) {
		for (Operator o: AllOperators) {
			Filter f = new Filter(a, new Range(o, BunchLib.list(provided)));
			Problems p = new Problems();
			Substitution est = new Substitution(p, f);
			if (!p.isOK())
				fail("problem detected during substitution: " + p.getProblemStrings());
			if (o == Operator.EQ) {
				assertSame(a, est.aspect);
				assertEquals(canReplace, est.canReplace);
				assertEquals(value, est.value);
			} else {
				assertEquals(false, est.canReplace);
				assertEquals(null, est.value);
			}
		}
	}

}
