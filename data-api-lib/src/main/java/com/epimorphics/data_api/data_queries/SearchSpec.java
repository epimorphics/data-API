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
import com.epimorphics.data_api.sparql.SQ_Const;
import com.epimorphics.data_api.sparql.SQ_Literal;
import com.epimorphics.data_api.sparql.SQ_Node;
import com.epimorphics.data_api.sparql.SQ_Resource;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ_Triple;
import com.epimorphics.data_api.sparql.SQ_Variable;
import com.epimorphics.data_api.sparql.SQ_WhereElement;
import com.hp.hpl.jena.rdf.model.Resource;
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

	public void tripleFiltering(Context cx) {
		cx.generateSearchSQ(this);
	}
	
	public Shortname getAspectName() {
		return aspectName;
	}

	public void toSearchTripleSQ(Context cx, Map<Shortname, Aspect> aspects, PrefixMapping pm) {
		if (aspectName == null) {
			SQ_Node O = asSQNode(pm);
			SQ_Triple t = new SQ_Triple(SQ_Const.item, SQ_Const.textQuery, O);
			if (negated) {
				cx.sq.addNotExists(t);
			} else {
				cx.sq.addTriple(t);			
			}
		} else {
			cx.sq.addWhereElement(toSearchAspectTripleSQ(aspects, pm));
		}
	}
	
	private SQ_Node asSQNode(PrefixMapping pm) {
		SQ_Literal literal = new SQ_Literal(pattern, "");
		if (property == null && limit == null) {
			return literal;		
		} else if (property == null) {
			return SQ.list(literal, SQ.integer(limit));
		} else {
// WAS:
//			String expanded = pm.expandPrefix(property.URI);
//			String contracted = pm.qnameFor(expanded);
//			String use = contracted == null ? "<" + property.URI + ">" : contracted;
//			return "(" + use + " " + quoted + limitString + ")";

			SQ_Resource useProperty = new SQ_Resource(property.URI);
			if (limit == null) return SQ.list(useProperty, literal);
			else return SQ.list(useProperty, literal, SQ.integer(limit));
		}
	}

	private SQ_WhereElement toSearchAspectTripleSQ(Map<Shortname, Aspect> aspects, PrefixMapping pm) {
		SQ_WhereElement positive = toPositiveSearchAspectTripleSQ(aspects, pm);
		if (negated) {
			throw new RuntimeException("TBD");
			// return " FILTER(NOT EXISTS{" + positive + "})"; 
		} else {
			return positive;
		}
	}
	
	private SQ_WhereElement toPositiveSearchAspectTripleSQ(Map<Shortname, Aspect> aspects, PrefixMapping pm) {
		Aspect a = aspects.get(aspectName);
		
//		System.err.println( ">> toSearchApsectTriple of " + aspectName );
//		System.err.println( ">>   aspect is " + a );
//		System.err.println( ">>   .pattern = " + pattern );
//		System.err.println( ">>   .aspectName = " + aspectName );
//		System.err.println( ">>   .property = " + property );
		
		SQ_Variable aVar = new SQ_Variable(aspectName.asVar().substring(1));
		boolean hasLiteralRange = hasLiteralRange(a);
		
		if (property == null) {
			
			if (hasLiteralRange) {

				return new SQ_Triple( SQ_Const.item, SQ_Const.textQuery, asSQNode(pm) );
				
			} else {
				
				return new SQ_Triple( aVar, SQ_Const.textQuery, asSQNode(pm) );
				
			}
			
		} else {
			
			if (hasLiteralRange) {

				throw new UnsupportedOperationException
					("@search on aspect " + a + " has @property " + property + " -- should have been detected earlier" );
				
			} else {
				
				return new SQ_Triple(aVar, SQ_Const.textQuery, asSQNode(pm));
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
