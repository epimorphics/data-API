/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.terms;

import com.epimorphics.data_api.data_queries.terms.Term.Primitive;
import com.epimorphics.json.JSFullWriter;
import org.apache.jena.shared.PrefixMapping;

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
	
	@Override public String asSparqlTerm(PrefixMapping pm) {
		return toString();
	}

	@Override public void writeMember(String key, JSFullWriter jw) {
		jw.pair(key, value);
	}

	@Override public void writeElement(JSFullWriter jw) {
		jw.arrayElement(value);
	}

	@Override public <T> T visit(Visitor<T> v) {
		return v.visitNumber(this);
	}
}