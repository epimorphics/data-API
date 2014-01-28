/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

/**
    A Term encodes a SPARQL term.
*/
public abstract class Term {
	
	public abstract String asSparqlTerm();
	
	@Override public abstract String toString();
	
	@Override public abstract int hashCode();
	
	@Override public abstract boolean equals(Object other);
	
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
		
	}
	
	public static class TermBool extends Term {
		
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
	}
	
	public static class TermString extends Term {

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
	}
	
	public static class TermNumber extends Term {

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
	}
	
	public static class TermTyped extends Term {

		final String value;
		final String type;
		
		public TermTyped(String value, String type) {
			this.value = value;
			this.type = type;
		}

		@Override public String toString() {
			return value.toString() + "::" + type.toString();
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
	}

	public static Term bool(boolean value) {
		return new TermBool(value);
	}

	public static Term string(String value) {
		return new TermString(value);
	}

	public static Term number(Number value) {
		return new TermNumber(value);
	}
	
	public static Term bad(Object problematic) {
		return new TermBad(problematic);
	}
	
	public static Term URI(String spelling) {
		return new TermResource(spelling);
	}
	
	public static Term typed(String spelling, String type) {
//		LiteralLabel ll = LiteralLabelFactory.create(spelling, "", new BaseDatatype(type));
//		Node n = NodeFactory.createLiteral(ll);
		return new TermTyped(spelling, type);
	}

//	public String old_asSparqlTerm() {
//		Object w = wrapped;
//		System.err.println( ">> TODO: asSparqlTerm needs proper definition." );
//		System.err.println( ">> w = (" + w + "), " + w.getClass().getSimpleName() );
//		if (w instanceof String) 
//			return "'" + w + "'";
//		if (w instanceof Node_Literal) {
//			Node_Literal nl = (Node_Literal) w;
//			String type = nl.getLiteralDatatypeURI();
//			return "'" + nl.getLiteralLexicalForm() + "'^^<" + type + ">";
//		}
//		if (w instanceof Node_URI) {
//			return "<" + ((Node_URI) w).getURI() + ">";
//		}
//		return w.toString();
//	}
}