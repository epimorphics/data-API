/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

/**
    A Term encodes a SPARQL term.
*/
public class Term {
	
	final Object wrapped;
	
	public Term(Object x) {
		this.wrapped = x;			
	}
	
	private static Term wrap(Object x) {
		return new Term(x);
	}
	
	public Object unwrap() {
		return wrapped;
	}

	public static Term bool(boolean value) {
		return wrap(value);
	}

	public static Term string(String value) {
		return wrap(value);
	}

	public static Term number(Number value) {
		return wrap(value);
	}
	
	public static Term bad(Object basis) {
		return wrap(basis);
	}
	
	public static Term URI(String spelling) {
		return wrap(NodeFactory.createURI(spelling));
	}
	
	public static Term typed(String spelling, String type) {
		LiteralLabel ll = LiteralLabelFactory.create(spelling, "", new BaseDatatype(type));
		Node n = NodeFactory.createLiteral(ll);
		return wrap(n);
	}
	
	@Override public String toString() {
		return wrapped.toString();
	}
	
	@Override public int hashCode() {
		return wrapped.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Term && wrapped.equals(((Term) other).wrapped);
	}

	public String asSparqlTerm() {
		Object w = wrapped;
		System.err.println( ">> TODO: asSparqlTerm needs proper definition." );
		System.err.println( ">> w = (" + w + "), " + w.getClass().getSimpleName() );
		if (w instanceof String) 
			return "'" + w + "'";
		if (w instanceof Node_Literal) {
			Node_Literal nl = (Node_Literal) w;
			String type = nl.getLiteralDatatypeURI();
			return "'" + nl.getLiteralLexicalForm() + "'^^<" + type + ">";
		}
		if (w instanceof Node_URI) {
			return "<" + ((Node_URI) w).getURI() + ">";
		}
		return w.toString();
	}
}