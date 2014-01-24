/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;

/**
    A Value wraps an Object. Only a limited set of classes of object
    can be Wrapped; they correspond to values that can conveniently be
    converted to SPARQL terms.
*/
public class Value {
	
	final Object wrapped;
	
	public Value(Object x) {
		this.wrapped = x;			
	}
	
	public static Value wrap(Object x) {
		return new Value(x);
	}
	
	public Object unwrap() {
		return wrapped;
	}
	
	@Override public String toString() {
		return wrapped.toString();
	}
	
	@Override public int hashCode() {
		return wrapped.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Value && wrapped.equals(((Value) other).wrapped);
	}

	public String asSparqlTerm() {
		Object w = wrapped;
		System.err.println( ">> TODO: asSparqlTerm needs proper definition." );
		System.err.println( ">> w = (" + w + "), " + w.getClass().getSimpleName() );
		if (w instanceof String) return "'" + w + "'";
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