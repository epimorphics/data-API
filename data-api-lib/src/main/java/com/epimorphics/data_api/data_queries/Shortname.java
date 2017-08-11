/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import org.apache.jena.shared.PrefixMapping;

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
	final String varName;
	
	public Shortname(PrefixMapping pm, String prefixed ) {
		this.prefixed = prefixed;
		this.URI = pm.expandPrefix(prefixed);
		this.varName = asVarName(prefixed);
	}
	
	static final char[] digit = "0123456789ABCDEF".toCharArray();
	
	public static String asVarName(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i += 1) {
			char ch = s.charAt(i);
			if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') 
				sb.append(ch);
			else if (ch == ':')
				sb.append('_');
			else
				sb.append(digit[ch >> 4]).append(digit[ch & 0xf]);
		}
		return sb.toString();
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
	
	@Override public String toString() {
		return prefixed + " [" + URI + "]";
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Shortname && same( (Shortname) other);
	}

	private boolean same(Shortname other) {
		return URI.equals(other.URI) && prefixed.equals(other.prefixed);
	}

	public String getVarName() {
		return varName;
	}

	public String asVar() {
		return "?" + varName;
	}
}