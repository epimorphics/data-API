/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.data_queries.Term.Primitive;
import com.epimorphics.json.JSFullWriter;

public class TermNumber extends Primitive {

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