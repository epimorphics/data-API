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
import com.epimorphics.data_api.data_queries.terms.TermArray;
import com.epimorphics.data_api.data_queries.terms.TermBad;
import com.epimorphics.data_api.data_queries.terms.TermBool;
import com.epimorphics.data_api.data_queries.terms.TermLanguaged;
import com.epimorphics.data_api.data_queries.terms.TermNumber;
import com.epimorphics.data_api.data_queries.terms.TermResource;
import com.epimorphics.data_api.data_queries.terms.TermString;
import com.epimorphics.data_api.data_queries.terms.TermTyped;
import com.epimorphics.data_api.data_queries.terms.TermVar;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;

public class TestTermTypeCompatability {

	final String succeed = "succeed";
	final String fail = "fail";
	
	static final List<Term> terms = new ArrayList<Term>();
	
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
		Term.Visitor tv = new Term.Visitor() {

			@Override public Term visitVar(TermVar tv) {
				p.add("variable term " + tv + " not permitted as value.");
				return null;
			}

			@Override public Term visitTyped(TermTyped tt) {
				return checkTyped(p, type, tt);
			}

			@Override public Term visitResource(TermResource tr) {
				return checkResource(p, type, tr);
			}

			@Override public Term visitLanguaged(TermLanguaged tl) {
				return checkLanguaged(p, type, tl);
			}

			@Override public Term visitBad(TermBad tb) {
				p.add("bad term " + tb + " not permitted as value.");
				return null;
			}

			@Override public Term visitArray(TermArray ta) {
				p.add("array term " + ta + " not permitted as value.");
				return null;
			}

			@Override public Term visitString(TermString ts) {
				return checkString(p, type, ts);
			}

			@Override public Term visitNumber(TermNumber tn) {
				return checkNumber(p, type, tn);
			}

			@Override public Term visitBool(TermBool tb) {
				return checkNumber(p, type, tb);
			}
		};
		Term result = t.visit(tv);
		if (mode.equals(succeed)) {
			if (p.size() > 0) fail(p.getProblemStrings());
		} else {
			if (p.size() == 0) fail("should have detected a problem.");
		}
	}

	protected Term checkTyped(Problems p, Resource type, TermTyped tt) {
		if (type == null) {
			// that's OK, nothing to check against.
		} else if (tt.getTypeURI().equals(type.getURI())){
			p.add("type of term " + tt + " inconsistent with range type " + type);
		}
		return null;
	}

	static final String RDF_langString = RDF.getURI() + "langString";
	
	protected Term checkLanguaged(Problems p, Resource type, TermLanguaged tl) {
		if (type == null) {
			// no type to clash with, so that's OK.
		} else if (type.getURI().equals(RDF_langString)) {
			// It has the appropriate type
		} else {
			p.add("string with language code " + tl + " has type " + type + " other than " + RDF_langString);
		}
		return null;
	}

	protected Term checkResource(Problems p, Resource type, TermResource tr) {
		if (type == null) {
			// that's fine, no type available so no inconsistency.
		} else if (isLiteralType(type)) {
			// the type expects its instances to be literals. Oops.
			p.add(
				"values of type " + type + " are expected to be literals"
				+ ", but " + tr + " is a resource."
				);
		} else {
			// that's also fine, the type is not (known to be) a literal type.
		}
		return null;
	}

	private boolean isLiteralType(Resource type) {
		return type != null && type.getURI().startsWith(XSD.getURI());
	}

	protected Term checkNumber(Problems p, Resource type, TermBool tb) {
		if (type.getURI().equals(XSDDatatype.XSDboolean.getURI())) {
			// given boolean type, that's fine
		} else {
			p.add("boolean " + tb + " given type " + type);
		}		
		return null;
	}

	// tn is a JSON numeric value so type should be a corresponding
	// numeric type. What should 'corresponding' mean? Well ... if
	// we construct a literal from the lexical form and the type, it
	// shouldn't raise an exception. So let's try that then.
	private Term checkNumber(Problems p, Resource type, TermNumber tn) {		
		RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(type.getURI());
		try {
			Object value = dt.parse(tn.toString());
			return tn;
		} catch (RuntimeException e) {
			p.add("OOPS: " + tn + " not acceptable as an instance of " + type);
			return null;
		}
	}

	static final boolean backwardsCompatibleStringTypes = true;
	
	private Term checkString(Problems p, Resource type, TermString ts) {
		if (backwardsCompatibleStringTypes) {
			if (type == null) {
				// untyped is fine for an old-fashioned plain string
			} else {
				p.add("plain string value " + ts + " given type " + type);
			}
		} else {
			if (type.getURI().equals(XSDDatatype.XSDstring.getURI())) {
			} else {
				p.add("string value " + ts + " is not expected for " + type);
			}
		}
		return null;
	}
}
