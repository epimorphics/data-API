/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries.terms;

import java.math.BigDecimal;
import java.util.List;

import com.epimorphics.data_api.conversions.Compactions;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.FmtUtils;

/**
    A Term encodes a Node or an array of Terms.
    It may be converted to a SPARQL term when constructing
    a query, or to JSON when rendering a result-set.
*/
public abstract class Term implements JSONWritable {
	
	public abstract String asSparqlTerm(PrefixMapping pm);
	
	public abstract <T> T visit(Visitor<T> v);
	
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
	
	public static Term fromNode(Compactions c, Node n) {
		if (n.isURI()) {
			String uri = n.getURI();
			return c.suppressTypes() ? Term.string(uri) : Term.URI(uri);			
		} else if (n.isLiteral()) {
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
			} else if (c.suppressTypes()) {
				return Term.string(spelling);
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
	
	public static String quote(String content) {
		return "\"" + FmtUtils.stringEsc(content, true) + "\"";
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
	
	public interface Visitor<T> {
		public T visitVar(TermVar tv);
		public T visitTyped(TermTyped tt);
		public T visitResource(TermResource tr);
		public T visitLanguaged(TermLanguaged tl);
		public T visitBad(TermBad tb);
		public T visitArray(TermArray ta);
		public T visitString(TermString ts);
		public T visitNumber(TermNumber tn);
		public T visitBool(TermBool tb);
	}
}