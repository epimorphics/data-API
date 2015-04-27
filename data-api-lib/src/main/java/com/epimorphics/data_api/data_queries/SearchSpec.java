/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.sparql.SQ_Const;
import com.epimorphics.data_api.sparql.SQ_Literal;
import com.epimorphics.data_api.sparql.SQ_Node;
import com.epimorphics.data_api.sparql.SQ_Resource;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ_Triple;
import com.epimorphics.data_api.sparql.SQ_Variable;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

public class SearchSpec extends Restriction {

	final String pattern;
	final Integer limit;
	final Shortname property;
	final boolean negated;
	final Aspect aspect;

	public SearchSpec(Aspect a, String pattern) {
		this(a, pattern, null, null);
	}

	public SearchSpec(Aspect a, String pattern, Shortname property) {
		this(a, pattern, property, null);
	}

	public SearchSpec(Aspect a, String pattern, Shortname property, Integer limit) {
		this(a, pattern, property, limit, false);
	}

	public SearchSpec(Aspect aspect, String pattern, Shortname property, Integer limit, boolean negated) {
		this.pattern = pattern;
		this.property = property;
		this.limit = limit;
		this.negated = negated;
		this.aspect = aspect;
	}

	/**
	    withExplicitField() returns a SearchSpec with the
	    field name of the spec explicitly present in the
	    pattern.
	*/
	public SearchSpec withExplicitField() {
		String aPattern = fieldName() + ": " + pattern;
		return new SearchSpec
			( aspect
			, aPattern
			, getAspectName()
			, limit
			);
	}

	/**
		withAndExplicitField(B) ANDs the pattern and
		field of B with the explicit-field pattern of this
		SearchSpec and returns the result. The limit field of the
		result is the maximum of the limit fields of this
		SearchSpec and B.			
	*/
	public SearchSpec withAndExplicitField(SearchSpec B) {
		String bField = B.fieldName(); 
		
		String jointPattern = 
			pattern + " AND " + bField + ": " + B.pattern;
			;		
		
		SearchSpec result = new SearchSpec
			( aspect
			, jointPattern
			, getAspectName()	
			, max(limit, B.limit)
			);
		
		return result;
	}

	private static Integer max(Integer A, Integer B) {
		if (A == null) return B;
		if (B == null) return A;
		return Math.max(A, B);
	}
	
	public boolean isGlobal() {
		return aspect == null;
	}
	
	public String getPattern() {
		return pattern;
	}
	
	/**
		fieldName() returns the name of the indexing field of
		this SearchSpec, defined as the local name of the
		property URI or, if that is null, the local name of
		the aspect's name. 
	*/
	public String fieldName() {
		return localName
			((hasSpecifiedProperty() ? property : aspect.getName()).URI)
			;
	}
	
	protected boolean hasSpecifiedProperty() {
		return 
			property != null 
			&& (aspect == null ? true : !property.equals(aspect.getName()))
			;
	}

	/*
		localName(uri) returns the local name of the uri, defined
		as the section of the URI starting just past the last
		'#' or '/', whichever is later in the URI. This is used
		to derive the field name from a property name.
	*/
	private String localName(String uri) {
		int lastSlash = uri.lastIndexOf('/');
		int lastHash = uri.lastIndexOf('#');
		int begin = Math.max(lastSlash, lastHash);
		return uri.substring(begin + 1);
	}
	
	@Override void applyTo(State s) {
		if (isGlobal()) {
			applyGlobalSearch(s.cx);
		} else {
			s.cx.sq.addTriple(toPositiveSearchAspectTriple());
		}
	}

	private void applyGlobalSearch(Context cx) {
		SQ_Node O = asSQNode();
		SQ_Triple t = new SQ_Triple(SQ_Const.item, SQ_Const.textQuery, O);
		if (negated) {
			cx.sq.addNotExists(t);
		} else {
			cx.sq.addTriple(t);			
		}
	}

	@Override public Constraint negate() {
		return new SearchSpec(aspect, pattern, property, limit, true);
	}
	
	public Shortname getAspectName() {
		return aspect == null ? null : aspect.getName();
	}
	
	private SQ_Node asSQNode() {
		SQ_Literal literal = new SQ_Literal(pattern, "");
		if (!hasSpecifiedProperty() && limit == null) {
			return literal;		
		} else if (!hasSpecifiedProperty()) {
			return SQ.list(literal, SQ.integer(limit));
		} else {
			SQ_Resource useProperty = new SQ_Resource(property.URI);
			if (limit == null) return SQ.list(useProperty, literal);
			else return SQ.list(useProperty, literal, SQ.integer(limit));
		}
	}

	private SQ_Triple toPositiveSearchAspectTriple() {
		SQ_Variable aVar = new SQ_Variable(aspect.asVar().substring(1));
		boolean hasLiteralRange = hasLiteralRange(aspect);
		
		if (hasLiteralRange && hasSpecifiedProperty()) {
			throw new UnsupportedOperationException
				("@search on aspect " + (aspect == null ? aspect : aspect) + " has @property " + property + " -- should have been detected earlier" );
		}
		
		if (hasLiteralRange) 
			return new SQ_Triple( SQ_Const.item, SQ_Const.textQuery, asSQNode() );
		else
			return new SQ_Triple(aVar, SQ_Const.textQuery, asSQNode());
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
		String match = Term.quote(pattern) + (hasSpecifiedProperty() ? ", " + property : "");
		String limitString = (limit == null ? "" : ", limit: " + limit );
		return
			(negated ? "@not " : "")
			+ "@search" 
			+ (aspect == null ? "[global]" : "[" + aspect + "]") 
			+ " (" + match + limitString + ")"
			;
	}
	
	@Override public int hashCode() {
		return
			(pattern == null ? 0 : pattern.hashCode())
			^ (hasSpecifiedProperty() ? property.hashCode() : 0)
			^ (aspect == null ? 0 : aspect.hashCode())
			;
	}

	@Override protected boolean same(Constraint other) {
		return same((SearchSpec) other);
	}
	
	private boolean same(SearchSpec other) {
		return
			(aspect == null ? other.aspect == null : aspect.equals(other.aspect))
			&& (pattern == null ? other.pattern == null : pattern.equals(other.pattern))
			&& (property == null ? other.property == null : property.equals(other.property))
			&& (limit == null ? other.limit == null : limit.equals(other.limit))
			;
	}

	public static SearchSpec absent() {
		return new SearchSpec(null, null);
	}

	public static List<SearchSpec> none() {
		return new ArrayList<SearchSpec>();
	}

	@Override protected boolean constrains(Aspect a) {
		return aspect == null ? false : aspect.equals(a);
	}	
}
