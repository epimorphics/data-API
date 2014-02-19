/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.math.BigDecimal;
import java.util.List;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

/**
    A Term encodes a Node or an array of Terms.
    It may be converted to a SPARQL term when constructing
    a query, or to JSON when rendering a result-set.
*/
public abstract class Term implements JSONWritable {
	
	public abstract String asSparqlTerm();
	
	@Override public abstract String toString();
	
	@Override public abstract int hashCode();
	
	@Override public abstract boolean equals(Object other);

	public void writeMember(String key, JSFullWriter jw) {
		jw.key(key);
		writeTo(jw);
	}
	
	public void writeElement(JSFullWriter jw) {
		writeTo(jw);
	}
	
	public static Term fromNode(Node n) {
		if (n.isURI()) 
			return Term.URI(n.getURI());
		if (n.isLiteral()) {
			String spelling = n.getLiteralLexicalForm();
			String type = n.getLiteralDatatypeURI();
			if (type == null) {
				String language = n.getLiteralLanguage();
				if (language.equals("")) {
					return Term.string(spelling);
				} else {
					return Term.languaged(spelling, language);
				}
			} else if (type.equals(XSDDatatype.XSDboolean.getURI())) {
				return Term.bool(spelling.equals("true"));
			} else if (type.equals(XSDDatatype.XSDinteger.getURI())) {
				return Term.integer(spelling);
			} else if (type.equals(XSDDatatype.XSDdecimal.getURI())) {
				return Term.decimal(spelling);				
			} else if (type.equals(XSDDatatype.XSDfloat.getURI())) {
				return Term.Double(spelling);				
			} else if (type.equals(XSDDatatype.XSDdouble.getURI())) {
				return Term.Double(spelling);
			} else if (type.equals(XSDDatatype.XSDint.getURI())) {
				return Term.integer(spelling);
			} else {
				return Term.typed(spelling, type);
			}
		}
		throw new RuntimeException("cannot handle this node: " + n);
	}

	protected abstract static class Primitive extends Term {

		@Override public void writeTo(JSFullWriter jw) {
			throw new UnsupportedOperationException(this.getClass().getSimpleName());
		}

	}
	
	public static Term bool(boolean value) {
		return new TermBool(value);
	}

	public static Term string(String value) {
		return new TermString(value);
	}

	public static Term integer(String spelling) {
		return new TermNumber(Integer.parseInt(spelling));
	}

	public static Term number(Number value) {
		return new TermNumber(value);
	}

	public static Term decimal(String spelling) {
		return new TermNumber(new BigDecimal(spelling));
	}

	public static Term Double(String spelling) {
		return new TermNumber(Double.parseDouble(spelling));
	}
	
	public static Term bad(Object problematic) {
		return new TermBad(problematic);
	}
	
	public static Term URI(String spelling) {
		return new TermResource(spelling);
	}
	
	public static Term var(String spelling) {
		return new TermVar(spelling);
	}
	
	public static Term languaged(String spelling, String lang) {
		return new TermLanguaged(spelling, lang);
	}
	
	public static Term typed(String spelling, String type) {
		return new TermTyped(spelling, type);
	}
	
	public static Term array(List<Term> terms) {
		return new TermArray(terms);
	}
}