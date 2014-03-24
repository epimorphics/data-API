/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.XSD;

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

	public String toSearchTriple(Map<Shortname, Aspect> aspects, PrefixMapping pm) {
		if (aspectName == null) {
			return
				"?item"
				+ " <http://jena.apache.org/text#query> "
				+ asSparqlTerm(pm)
				;			
		} else {
			return toSearchAspectTriple(aspects, pm);
		}
	}

	private String toSearchAspectTriple(Map<Shortname, Aspect> aspects, PrefixMapping pm) {
		Aspect a = aspects.get(aspectName);
		
//		System.err.println( ">> toSearchApsectTriple of " + aspectName );
//		System.err.println( ">>   aspect is " + a );
//		System.err.println( ">>   .pattern = " + pattern );
//		System.err.println( ">>   .aspectName = " + aspectName );
//		System.err.println( ">>   .property = " + property );
		
		boolean hasLiteralRange = hasLiteralRange(a);
		if (property == null) {
			
			if (hasLiteralRange) {

				return
					("?item")
					+ " <http://jena.apache.org/text#query> "
					+ asSparqlTerm(pm)
					;
				
			} else {
				
				return
					("?" + aspectName.asVar())
					+ " <http://jena.apache.org/text#query> "
					+ Term.quote(pattern)
					;
				
			}
			
		} else {
			
			if (hasLiteralRange) {

				throw new UnsupportedOperationException
				("@search on aspect " + a + " has @property " + property + " -- should have been detected earlier" );
				
			} else {
				
				return
					("?" + aspectName.asVar())
					+ " <http://jena.apache.org/text#query> "
					+ asSparqlTerm(pm)
					;
			}
			
		}
	}

	private boolean hasLiteralRange(Aspect a) {
		return isLiteralType(a.getRangeType());
	}

	private boolean isLiteralType(Resource type) {
		return type == null || type.getURI().startsWith(XSD.getURI());
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
