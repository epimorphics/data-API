/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.shared.PrefixMapping;

public class SearchSpec {

	private final String pattern;
	private final Shortname aspectName;
	private final Shortname property;
	
	public SearchSpec(String pattern) {
		this(pattern, null);
	}

	public SearchSpec(String pattern, Shortname aspectName) {
		this(pattern, aspectName, null);
	}

	public SearchSpec(String pattern, Shortname aspectName, Shortname property) {
		this.pattern = pattern;
		this.property = property;
		this.aspectName = aspectName;
	}

	public String asSparqlTerm(PrefixMapping pm) {
		String quoted = Term.quote(pattern);
		if (property == null) {
			return quoted;			
		} else {
			String expanded = pm.expandPrefix(property.URI);
			String contracted = pm.qnameFor(expanded);
			String use = contracted == null ? "<" + property.URI + ">" : contracted;
			return "(" + use + " " + quoted + ")";		
		}
	}

	public String toSearchTriple(PrefixMapping pm) {
		return
			(aspectName == null ? "?item" : "?" + aspectName.asVar())
			+ " <http://jena.apache.org/text#query> "
			+ asSparqlTerm(pm)
			;
	}

	@Override public String toString() {
		if (pattern == null) return "absent @search";
		String match = Term.quote(pattern) + (property == null ? "" : property);
		return "@search" + (aspectName == null ? "[global]" : "[" + aspectName + "]") + "(" + match + ")";
	}
	
	@Override public int hashCode() {
		return
			(pattern == null ? 0 : pattern.hashCode())
			^ (property == null ? 0 : property.hashCode())
			^ (aspectName == null ? 0 : aspectName.hashCode())
			;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof SearchSpec && same( (SearchSpec) other );
	}

	private boolean same(SearchSpec other) {
		return
			(aspectName == null ? other.aspectName == null : aspectName.equals(other.aspectName))
			&& (pattern == null ? other.pattern == null : pattern.equals(other.pattern))
			&& (property == null ? other.property == null : property.equals(other.property))
			;
	}

	public boolean isPresent() {
		return pattern != null;
	}

	public static SearchSpec absent() {
		return new SearchSpec(null, null);
	}

	public static List<SearchSpec> none() {
		return new ArrayList<SearchSpec>();
	}
}
