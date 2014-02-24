/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.data_queries.Term.Primitive;
import com.epimorphics.json.JSFullWriter;

public class TermBool extends Primitive {
	
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

	@Override public void writeMember(String key, JSFullWriter jw) {
		jw.pair(key, value);
	}

	@Override public void writeElement(JSFullWriter jw) {
		jw.arrayElement(value);
	}
}