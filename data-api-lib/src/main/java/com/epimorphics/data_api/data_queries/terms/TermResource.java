/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries.terms;

import com.epimorphics.json.JSFullWriter;
import org.apache.jena.shared.PrefixMapping;

public class TermResource extends TermComposite {
	
	public TermResource(String value) {
		super(value);
	}

	@Override public String toString() {
		return value.toString();
	}
	
	@Override public int hashCode() {
		return value.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof TermResource && value.equals(((TermResource) other).value);
	}
	
	@Override public String asSparqlTerm(PrefixMapping pm) {
		return "<" + value + ">";
	}

	@Override public void writeTo(JSFullWriter jw) {
		jw.startObject();
		jw.pair("@id", value);
		jw.finishObject();					
	}

	@Override public <T> T visit(Visitor<T> v) {
		return v.visitResource(this);
	}
}