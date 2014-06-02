/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ.Node;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.XSD;

public class SearchSpec extends Constraint {

	final String pattern;
	final Integer limit;
	private final Shortname aspectName;
	final Shortname property;
	final boolean negated;
	
	public SearchSpec(String pattern) {
		this(pattern, null);
	}

	public SearchSpec(String pattern, Shortname aspectName) {
		this(pattern, aspectName, null);
	}

	public SearchSpec(String pattern, Shortname aspectName, Shortname property) {
		this(pattern, aspectName, property, null);
	}

	public SearchSpec(String pattern, Shortname aspectName, Shortname property, Integer limit) {
		this(pattern, aspectName, property, limit, false);
	}

	public SearchSpec(String pattern, Shortname aspectName, Shortname property, Integer limit, boolean negated) {
		this.pattern = pattern;
		this.property = property;
		this.aspectName = aspectName;
		this.limit = limit;
		this.negated = negated;
	}

	@Override public Constraint negate() {
		return new SearchSpec(pattern, aspectName, property, limit, true);
	}

	@Override public void toSparql(Context cx, String varSuffix) {
		cx.generateSearch(this);
	}

	@Override public void toFilterBody(Context cx, String varSuffix) {
		throw new BrokenException("Search as FilterBody");
	}

	public void tripleFiltering(Context cx) {
		cx.generateSearch(this);
	}
	
	public Shortname getAspectName() {
		return aspectName;
	}

	public void toSearchTripleSQ(Context cx, Map<Shortname, Aspect> aspects, PrefixMapping pm) {
		if (aspectName == null) {
			if (negated) {
				// "FILTER(NOT EXISTS{?item  <http://jena.apache.org/text#query> " + asSparqlTerm(pm) + "})";
				throw new RuntimeException("TBD");
			} else {
				SQ.Node item = new SQ.Variable("item");
				SQ.Node textQuery = new SQ.Resource("http://jena.apache.org/text#query");
				SQ.Node O = asSQNode(pm);
				cx.sq.addTriple(new SQ.Triple(item, textQuery, O));
				
//				return
//					"?item"
//					+ " <http://jena.apache.org/text#query> "
//					+ asSparqlTerm(pm)
//					;			
			}
		} else {
			throw new RuntimeException("TBD");
			// return toSearchAspectTriple(aspects, pm);
		}
	}
	
	private Node asSQNode(PrefixMapping pm) {
		String limitString = (limit == null ? "" : " " + limit);
		SQ.Literal literal = new SQ.Literal(pattern, "");
		if (property == null && limit == null) {
			return literal;		
		} else if (property == null) {
			return SQ.list(literal, SQ.integer(limit));
		} else {
//			String expanded = pm.expandPrefix(property.URI);
//			String contracted = pm.qnameFor(expanded);
//			String use = contracted == null ? "<" + property.URI + ">" : contracted;
//			return "(" + use + " " + quoted + limitString + ")";
			throw new RuntimeException("TBD");
		}
	}

	public String toSearchTriple(Map<Shortname, Aspect> aspects, PrefixMapping pm) {
		if (aspectName == null) {
			if (negated) {
				return
					"FILTER(NOT EXISTS{?item  <http://jena.apache.org/text#query> " + asSparqlTerm(pm) + "})";				
			} else {
				return
					"?item"
					+ " <http://jena.apache.org/text#query> "
					+ asSparqlTerm(pm)
					;			
			}
		} else {
			return toSearchAspectTriple(aspects, pm);
		}
	}

	private String asSparqlTerm(PrefixMapping pm) {
		String quoted = Term.quote(pattern);
		String limitString = (limit == null ? "" : " " + limit);
		if (property == null && limit == null) {
			return quoted;		
		} else if (property == null) {
			return "(" + quoted + limitString + ")";
		} else {
			String expanded = pm.expandPrefix(property.URI);
			String contracted = pm.qnameFor(expanded);
			String use = contracted == null ? "<" + property.URI + ">" : contracted;
			return "(" + use + " " + quoted + limitString + ")";		
		}
	}

	private String toSearchAspectTriple(Map<Shortname, Aspect> aspects, PrefixMapping pm) {
		String positive = toPositiveSearchAspectTriple(aspects, pm);
		if (negated) {
			return " FILTER(NOT EXISTS{" + positive + "})"; 
		} else {
			return positive;
		}
	}

	private String toPositiveSearchAspectTriple(Map<Shortname, Aspect> aspects, PrefixMapping pm) {
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
					"?item"
					+ " <http://jena.apache.org/text#query> "
					+ asSparqlTerm(pm)
					;
				
			} else {
				
				return
					aspectName.asVar()
					+ " <http://jena.apache.org/text#query> "
					+ asSparqlTerm(pm)
					;
				
			}
			
		} else {
			
			if (hasLiteralRange) {

				throw new UnsupportedOperationException
					("@search on aspect " + a + " has @property " + property + " -- should have been detected earlier" );
				
			} else {
				
				return
					aspectName.asVar()
					+ " <http://jena.apache.org/text#query> "
					+ asSparqlTerm(pm)
					;
			}
			
		}
	}

	public boolean hasLiteralRange(Aspect a) {
		if (a == null) return true;
		Resource rangeType = a.getRangeType();
		if (rangeType == null) return false;
		return isLiteralType(rangeType);
	}

	private boolean isLiteralType(Resource type) {
		return type != null && type.getURI().startsWith(XSD.getURI());
	}

	@Override public String toString() {
		if (pattern == null) return "absent @search";
		String match = Term.quote(pattern) + (property == null ? "" : ", " + property);
		String limitString = (limit == null ? "" : ", limit: " + limit );
		return
			(negated ? "@not " : "")
			+ "@search" 
			+ (aspectName == null ? "[global]" : "[" + aspectName + "]") 
			+ " (" + match + limitString + ")"
			;
	}
	
	@Override public int hashCode() {
		return
			(pattern == null ? 0 : pattern.hashCode())
			^ (property == null ? 0 : property.hashCode())
			^ (aspectName == null ? 0 : aspectName.hashCode())
			;
	}

	@Override protected boolean same(Constraint other) {
		return same((SearchSpec) other);
	}
	
	private boolean same(SearchSpec other) {
		return
			(aspectName == null ? other.aspectName == null : aspectName.equals(other.aspectName))
			&& (pattern == null ? other.pattern == null : pattern.equals(other.pattern))
			&& (property == null ? other.property == null : property.equals(other.property))
			&& (limit == null ? other.limit == null : limit.equals(other.limit))
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
