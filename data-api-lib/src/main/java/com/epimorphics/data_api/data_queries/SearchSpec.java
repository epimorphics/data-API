/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

public class SearchSpec {

	private final String pattern;
	private final String property;
	
	public SearchSpec(String pattern) {
		this(pattern, null);
	}

	public SearchSpec(String pattern, String property) {
		this.pattern = pattern;
		this.property = property;
	}

	public String asSparqlTerm() {
		return Term.quote(pattern);
	}
	
	@Override public String toString() {
		if (pattern == null) return "absent @search";
		if (property == null) return "@search(" + Term.quote(pattern) + ")";
		return "@search(" + Term.quote(pattern) + ", " + property + ")";
	}
	
	@Override public int hashCode() {
		return
			(pattern == null ? 0 : pattern.hashCode())
			^ (property == null ? 0 : property.hashCode())
			;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof SearchSpec && same( (SearchSpec) other );
	}

	private boolean same(SearchSpec other) {
		return
			(pattern == null ? other.pattern == null : pattern.equals(other.pattern))
			&& (property == null ? other.property == null : property.equals(other.property))
			;
	}

	public boolean isPresent() {
		return pattern != null;
	}

	public static SearchSpec absent() {
		return new SearchSpec(null, null);
	}
}
