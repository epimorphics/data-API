/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.terms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

/**
    A TermValidator checks that given Term is compatible
    with a given type, reporting any problems.
*/
public final class TermValidator implements Term.Visitor<Term> {
	
	static Logger log = LoggerFactory.getLogger(TermValidator.class);

	static final String RDF_langString = RDF.getURI() + "langString";
	static final boolean backwardsCompatibleStringTypes = true;
	
	private final String name;
	private final Resource type;
	private final Problems p;
	private final API_Dataset api;

	/**
	   Initialise this TermValidator to check against type and
	   report any problems to p.
	*/
	public TermValidator(API_Dataset api, String name, Resource type, Problems p) {
		this.p = p;
		this.api = api;
		this.name = name;
		this.type = type;
	}

	public static void validate(API_Dataset api, Problems p, String name, Resource type, Term t) {
		Term.Visitor<Term> tv = new TermValidator(api, name, type, p);
		@SuppressWarnings("unused") Term result = t.visit(tv);
		if (p.size() > 0) {
			log.debug("rejected " + t + " for " + name);
		}
	}

	@Override public Term visitVar(TermVar tv) {
		p.add("variable term " + tv + " not permitted as value.");
		return null;
	}

	@Override public Term visitBad(TermBad tb) {
		p.add("bad term " + tb + " not permitted as value.");
		return null;
	}

	@Override public Term visitArray(TermArray ta) {
		p.add("array term " + ta + " not permitted as value.");
		return null;
	}

	private void report(Term t) {
		String typeName = api.getPrefixes().qnameFor(type.getURI());
		boolean literal = api.isLiteralType(type);
		String tString = t.asSparqlTerm(api.getPrefixes());
		p.add(
			"value " + tString + 
			" for aspect " + name 
			+ " is incompatible with" + (literal ? " literal" : "") + " range type " + typeName
			);
	}
	
	@Override public Term visitTyped(TermTyped tt) {
		if (type == null) return null;
//		thisCheckLooksBroken();
		if (tt.getTypeURI().equals(type.getURI())) report(tt);
		return null;
	}

	@Override public Term visitResource(TermResource tr) {
		if (type == null) return null;
		if (api.isLiteralType(type)) report(tr);
		return null;
	}

	@Override public Term visitLanguaged(TermLanguaged tl) {
		if (type == null) return null;
		if (!type.getURI().equals(RDF_langString)) {
			p.add("string with language code " + tl + " has type " + type + " other than " + RDF_langString);
		}
		return null;
	}

	@Override public Term visitString(TermString ts) {
		if (backwardsCompatibleStringTypes) {
			if (type != null) report(ts);
		} else if (!type.getURI().equals(XSDDatatype.XSDstring.getURI())) {
			p.add("string value " + ts + " is not expected for " + type);
		}
		return null;
	}

	@Override public Term visitNumber(TermNumber tn) {
		if (type == null) return null;
		RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(type.getURI());
		try {
			@SuppressWarnings("unused") Object value = dt.parse(tn.toString());
			return tn;
		} catch (RuntimeException e) {
			report(tn);
			return null;
		}
	}

	@Override public Term visitBool(TermBool tb) {
		if (type == null) return null;
		if (!type.getURI().equals(XSDDatatype.XSDboolean.getURI())) report(tb);
		return null;
	}

	protected Term checkTyped(Resource type, TermTyped tt) {	
		if (type == null) return null;
//		thisLooksIffyToMe();
		if (tt.getTypeURI().equals(type.getURI())) report(tt);
		return null;
	}
	
}