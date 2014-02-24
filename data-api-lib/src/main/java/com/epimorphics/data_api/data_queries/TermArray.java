/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.List;

import com.epimorphics.json.JSFullWriter;
import com.hp.hpl.jena.shared.PrefixMapping;

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

	@Override public String asSparqlTerm(PrefixMapping pm) {
		throw new UnsupportedOperationException("Cannot represent an array as a SPARQL term.");
	}

	@Override public void writeTo(JSFullWriter jw) {
		jw.startArray();
		for (Term t: terms) t.writeElement(jw);
		jw.finishArray();
	}
}