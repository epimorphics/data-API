/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.json.JSFullWriter;

public class TermTyped extends Term {

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