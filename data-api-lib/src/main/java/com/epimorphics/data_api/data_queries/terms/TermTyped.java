/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.terms;

import com.epimorphics.json.JSFullWriter;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TermTyped extends TermComposite {

	final String type;
	
	public TermTyped(String value, String type) {
		super(value);
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

	/**
	    asSparqlTerm returns a SPARQL literal term with the lexical
	    form taken from <code>value</code> and the type from <code>type</code>.
	    The type is represented as a URI reference unless it can be treated
	    as a prefix form, in which case that's what's used.
	*/
	@Override public String asSparqlTerm(PrefixMapping pm) {
		String expanded = pm.expandPrefix(type);
		String contracted = pm.qnameFor(expanded);
		if (contracted == null) throw new RuntimeException(">> OH BOTHER, involving " + type);
		return 
			quote(value) + "^^" 
			+ (contracted == null ? "<" + expanded + ">" : contracted)
			;
	}

	@Override public void writeTo(JSFullWriter jw) {
		jw.startObject();
		jw.pair("@value", value);
		jw.pair("@type", type);
		jw.finishObject();
	}
}