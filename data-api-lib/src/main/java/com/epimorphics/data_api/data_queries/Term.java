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
    A Term encodes a SPARQL term.
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

	public static class TermBad extends Term {
		
		final Object problematic;
		
		public TermBad(Object problematic) {
			this.problematic = problematic;
		}

		@Override public String toString() {
			return problematic.toString();
		}
		
		@Override public int hashCode() {
			return problematic.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermBad && problematic.equals(((TermBad) other).problematic);
		}
		
		@Override public String asSparqlTerm() {
			return "BAD(" + problematic + ")";
		}

		@Override public void writeTo(JSFullWriter out) {
			throw new UnsupportedOperationException("Cannot write bad Term");
		}
	}
	
	protected abstract static class Primitive extends Term {

		@Override public void writeTo(JSFullWriter jw) {
			throw new UnsupportedOperationException(this.getClass().getSimpleName());
		}

	}
	
	public static class TermBool extends Primitive {
		
		final boolean value;
		
		public TermBool(boolean value) {
			this.value = value;
		}

		@Override public String toString() {
			return value ? "true" : "false";
		}
		
		@Override public int hashCode() {
			return value ? 0 : 1;
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermBool && value == ((TermBool) other).value;
		}
		
		@Override public String asSparqlTerm() {
			return toString();
		}

		@Override public void writeMember(String key, JSFullWriter jw) {
			jw.pair(key, value);
		}

		@Override public void writeElement(JSFullWriter jw) {
			jw.arrayElement(value);
		}
	}
	
	public static class TermString extends Primitive {

		final String value;
		
		public TermString(String value) {
			this.value = value;
		}

		@Override public String toString() {
			return value.toString();
		}
		
		@Override public int hashCode() {
			return value.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermString && value.equals(((TermString) other).value);
		}
		
		@Override public String asSparqlTerm() {
			// TODO escaping
			return "'" + value + "'";
		}

		@Override public void writeMember(String key, JSFullWriter jw) {
			jw.pair(key, value);
		}

		@Override public void writeElement(JSFullWriter jw) {
			jw.arrayElement(value);
		}
	}
	
	public static class TermNumber extends Primitive {

		final Number value;
		
		public TermNumber(Number value) {
			this.value = value;
		}

		@Override public String toString() {
			return value.toString();
		}
		
		@Override public int hashCode() {
			return value.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermNumber && value.equals(((TermNumber) other).value);
		}
		
		@Override public String asSparqlTerm() {
			return toString();
		}

		@Override public void writeMember(String key, JSFullWriter jw) {
			jw.pair(key, value);
		}

		@Override public void writeElement(JSFullWriter jw) {
			jw.arrayElement(value);
		}
	}
	
	public static class TermVar extends Term {

		final String name;
		
		public TermVar(String name) {
			this.name = name;
		}

		@Override public String toString() {
			return "<var " + name + ">";
		}
		
		@Override public int hashCode() {
			return name.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermVar && name.equals(((TermVar) other).name);
		}
		
		@Override public String asSparqlTerm() {
			return "?" + name;
		}

		@Override public void writeTo(JSFullWriter out) {
			throw new UnsupportedOperationException("Cannot write variables to JSON.");
		}
	}
	
	public static class TermResource extends Term {

		final String value;
		
		public TermResource(String value) {
			this.value = value;
		}

		@Override public String toString() {
			return value.toString();
		}
		
		@Override public int hashCode() {
			return value.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermResource && value.equals(((TermResource) other).value);
		}
		
		@Override public String asSparqlTerm() {
			return "<" + value + ">";
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.startObject();
			jw.pair("@id", value);
			jw.finishObject();			
		}
	}
	
	public static class TermTyped extends Term {

		final String value;
		final String type;
		
		public TermTyped(String value, String type) {
			this.value = value;
			this.type = type;
		}

		@Override public String toString() {
			return value.toString() + "^^" + type.toString();
		}
		
		@Override public int hashCode() {
			return value.hashCode() ^ type.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermTyped && same((TermTyped) other);
		}
		
		private boolean same(TermTyped other) {
			return value.equals(other.value) && type.equals(other.type);
		}

		@Override public String asSparqlTerm() {
			// TODO handle prefixing
			return "'" + value + "'^^" + "<" + type + ">";
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.startObject();
			jw.pair("@value", value);
			jw.pair("@type", type);
			jw.finishObject();
		}
	}

	
	public static class TermLanguaged extends Term {

		final String value;
		final String lang;
		
		public TermLanguaged(String value, String type) {
			this.value = value;
			this.lang = type;
		}

		@Override public String toString() {
			return value.toString() + "@" + lang.toString();
		}
		
		@Override public int hashCode() {
			return value.hashCode() ^ lang.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermLanguaged && same((TermLanguaged) other);
		}
		
		private boolean same(TermLanguaged other) {
			return value.equals(other.value) && lang.equals(other.lang);
		}

		@Override public String asSparqlTerm() {
			// TODO handle prefixing
			return "'" + value + "'@" + lang;
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.startObject();
			jw.pair("@value", value);
			jw.pair("@language", lang);
			jw.finishObject();
		}
	}	
	
	public static class TermArray extends Term {

		final List<Term> terms;
		
		public TermArray(List<Term> terms) {
			this.terms = terms;
		}

		@Override public String toString() {
			return terms.toString();
		}
		
		@Override public int hashCode() {
			return terms.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof TermArray && same((TermArray) other);
		}
		
		private boolean same(TermArray other) {
			return terms.equals(other.terms);
		}

		@Override public String asSparqlTerm() {
			throw new UnsupportedOperationException("Cannot represent an array as a SPARQL term.");
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.startArray();
			for (Term t: terms) t.writeElement(jw);
			jw.finishArray();
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