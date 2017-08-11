/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.terms;

import java.util.List;

import com.epimorphics.json.JSFullWriter;
import org.apache.jena.shared.PrefixMapping;

public class TermArray extends Term {

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
	
	/**
		size() returns the number of elements in the term list.
	*/
	public int size() {
		return terms.size();
	}
	
	/**
	    get(i) returns the i'th element of the term array if
	    0 <= i < size(). 
	*/
	public Term get(int i) {
		return terms.get(i);
	}

	@Override public String asSparqlTerm(PrefixMapping pm) {
		throw new UnsupportedOperationException("Cannot represent an array as a SPARQL term.");
	}

	@Override public void writeTo(JSFullWriter jw) {
		jw.startArray();
		for (Term t: terms) {
			t.writeElement(jw); 
		}
		jw.finishArray();
	}

	@Override public <T> T visit(Visitor<T> v) {
		return v.visitArray(this);
	}
}