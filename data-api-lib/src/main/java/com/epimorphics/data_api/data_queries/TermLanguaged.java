/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.json.JSFullWriter;

public class TermLanguaged extends Term {

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