/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.hp.hpl.jena.shared.PrefixMapping;

/**
    A Shortname holds the full URI and the CURIE of an Aspect
    (or other named thing) in the dataset. [We could probably have 
    got away with just using String, but then they would be easily
    confusable with other string-shaped things.)
    
    Hmm, probably needs to allow for direct specification; a 
    property-path aspect isn't just a single aspect property.
*/
public class Shortname {
	
	final String URI;
	final String prefixed;
	
	public Shortname(PrefixMapping pm, String prefixed ) {
		this.prefixed = prefixed;
		this.URI = pm.expandPrefix(prefixed);
	}
	
	public String getCURIE() {
		return prefixed;
	}
	
	public String getURI() {
		return URI;
	}
	
	@Override public int hashCode() {
		return URI.hashCode() ^ prefixed.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Shortname && same( (Shortname) other);
	}

	private boolean same(Shortname other) {
		return URI.equals(other.URI) && prefixed.equals(other.prefixed);
	}

	public String asVar() {
		return "?" + prefixed.replace(":", "_");
	}
}