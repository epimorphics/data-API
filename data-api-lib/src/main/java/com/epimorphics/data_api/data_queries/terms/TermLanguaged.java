/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.terms;

import com.epimorphics.json.JSFullWriter;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TermLanguaged extends TermComposite {

	final String lang;
	
	public TermLanguaged(String value, String lang) {
		super(value);
		this.lang = lang;
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

	@Override public String asSparqlTerm(PrefixMapping pm) {
		return quote(value) + "@" + lang;
	}

	@Override public void writeTo(JSFullWriter jw) {
		jw.startObject();
		jw.pair("@value", value);
		jw.pair("@lang", lang);
		jw.finishObject();
	}
}