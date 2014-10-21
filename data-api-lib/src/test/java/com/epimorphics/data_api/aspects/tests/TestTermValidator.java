/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.data_queries.terms.TermValidator;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestTermValidator {

	final String succeed = "succeed";
	final String fail = "fail";
	
	static final List<Term> terms = new ArrayList<Term>();
	
	// fake config so that getting the root works.
	static Resource config = ModelFactory
		.createDefaultModel()
		.createResource("eh:/root")
		;
	
	static final API_Dataset dataset = new API_Dataset(config, null);
	
	// testing terms to see if they're compatible with types.
	// this is just scratchpad code for the moment.
	@Test public void testType() {		
		testType(succeed, "xsd:integer", Term.integer("17"));
		testType(fail, "xsd:integer", Term.decimal("1.2"));
	//
		testType(succeed, "xsd:decimal", Term.integer("17"));
		testType(succeed, "xsd:decimal", Term.decimal("1.2"));
	//
		testType(fail, "xsd:string", Term.string("chatterbox"));
		testType(succeed, (Resource) null, Term.string("chatterbox"));
	//
		testType(fail, "xsd:decimal", Term.bad("iffy"));
		testType(fail, (Resource) null, Term.bad("iffy"));
	//	
		testType(fail, "xsd:decimal", Term.array(terms));
		testType(fail, (Resource) null, Term.array(terms));
	//
		testType(fail, "xsd:decimal", Term.var("name"));
		testType(fail, (Resource) null, Term.var("name"));
	//
		testType(fail, "xsd:decimal", Term.URI("eh:/some-resource"));
		testType(succeed, "eh:/some-type", Term.URI("eh:/another-resource"));
		testType(succeed, (Resource) null, Term.URI("eh:/extra-resource"));
	//
		testType(succeed, "xsd:boolean", Term.bool(true));
		testType(fail, "xsd:integer", Term.bool(false));
		testType(fail, "xsd:string", Term.bool(false));
	//
		testType(succeed, (Resource) null, Term.languaged("chat", "en"));
		testType(succeed, "rdf:langString", Term.languaged("chat", "en"));
		testType(fail, "xsd:integer", Term.languaged("chat", "en"));
	//
		testType(succeed, (Resource) null, Term.typed("17", "xsd:integer"));
//		testType(succeed, (Resource) null, Term.typed("17", "xsd:integer"));
	}
	
	Model m = ModelFactory.createDefaultModel();

	private void testType(String mode, String typeString, Term t) {
		Resource type = m.createResource(PrefixMapping.Standard.expandPrefix(typeString));
		testType(mode, type, t);
	}

	private void testType(String mode, final Resource type, Term t) {
		final Problems p = new Problems();
		TermValidator.validate(dataset, p, "anAspect", type, t);
		if (mode.equals(succeed)) {
			if (p.size() > 0) fail(p.getProblemStrings());
		} else {
			if (p.size() == 0) fail("should have detected a problem.");
		}
	}
}
