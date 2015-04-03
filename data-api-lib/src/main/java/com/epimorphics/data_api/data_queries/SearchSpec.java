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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.XSD;

public class SearchSpec extends Restriction {

	final String pattern;
	final Integer limit;
	private final Shortname aspectName;
	final Shortname property;
	final boolean negated;
	final Aspect aspect;
	
	public SearchSpec(Aspect a, String pattern) {
		this(a, pattern, null);
	}

	public SearchSpec(Aspect a, String pattern, Shortname aspectName) {
		this(a, pattern, aspectName, null);
	}

	public SearchSpec(Aspect a, String pattern, Shortname aspectName, Shortname property) {
		this(a, pattern, aspectName, property, null);
	}

	public SearchSpec(Aspect a, String pattern, Shortname aspectName, Shortname property, Integer limit) {
		this(a, pattern, aspectName, property, limit, false);
	}

	public SearchSpec(Aspect aspect, String pattern, Shortname aspectName, Shortname property, Integer limit, boolean negated) {
		this.pattern = pattern;
		this.property = property;
		this.aspectName = aspectName;
		this.limit = limit;
		this.negated = negated;
		this.aspect = aspect;
		
	//
	// sanity check while changing code to eliminate aspectName in
	// favour of aspect
		
		if (aspect == null) {
			if (aspectName == null) {
				// consistent
			} else {
				throw new BrokenException("aspect null but name is " + aspectName);
			}
		} else {
			if (aspectName == null) {
				throw new BrokenException("aspect is " + aspect + " but name is null");
			} else {
				if (aspect.getName().equals(aspectName)) {
					// consistent
				} else {
					throw new BrokenException("aspec is " + aspect + " but name is " + aspectName);
				}
			}
		}
	}
	
	@Override void applyTo(State s) {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		PrintStream ps = new PrintStream(bos);
//		new RuntimeException("stack").printStackTrace(ps);
//		ps.close();
//		System.err.println(">> STACK:\n" + bos.toString().substring(0, 700) + "\n...\n");
//		
		s.cx.sq.addTriple(toPositiveSearchAspectTriple(aspect));		
	}

	@Override public Constraint negate() {
		return new SearchSpec(aspect, pattern, aspectName, property, limit, true);
	}
	
	public Shortname getAspectName() {
		return aspectName;
	}

	public void toSearchTripleSQ(Context cx, PrefixMapping pm) {
		if (aspectName == null) {
			SQ_Node O = asSQNode();
			SQ_Triple t = new SQ_Triple(SQ_Const.item, SQ_Const.textQuery, O);
			if (negated) {
				cx.sq.addNotExists(t);
			} else {
				cx.sq.addTriple(t);			
			}
		} else {
			// TODO have an "early" triple.
			cx.sq.addTriple(toSearchAspectTripleSQ());
		}
	}
	
	private SQ_Node asSQNode() {
		SQ_Literal literal = new SQ_Literal(pattern, "");
		if (property == null && limit == null) {
			return literal;		
		} else if (property == null) {
			return SQ.list(literal, SQ.integer(limit));
		} else {
			SQ_Resource useProperty = new SQ_Resource(property.URI);
			if (limit == null) return SQ.list(useProperty, literal);
			else return SQ.list(useProperty, literal, SQ.integer(limit));
		}
	}

	private SQ_Triple toSearchAspectTripleSQ() {
		SQ_Triple positive = toPositiveSearchAspectTripleSQ();
		if (negated) {
			throw new RuntimeException("TBD");
			// return " FILTER(NOT EXISTS{" + positive + "})"; 
		} else {
			return positive;
		}
	}
	
	private SQ_Triple toPositiveSearchAspectTripleSQ() {
//		Aspect a = aspects.get(aspectName);	
//		
//		if (a.equals(aspect)) {} else {
//			throw new BrokenException("Found aspect not the same as searched aspect.");
//		}
		
		return toPositiveSearchAspectTriple(aspect);
	}

	private SQ_Triple toPositiveSearchAspectTriple(Aspect a) {
		SQ_Variable aVar = new SQ_Variable(aspectName.asVar().substring(1));
		boolean hasLiteralRange = hasLiteralRange(a);
		
		if (property == null) {
			
			if (hasLiteralRange) {

				return new SQ_Triple( SQ_Const.item, SQ_Const.textQuery, asSQNode() );
				
			} else {
				
				return new SQ_Triple( aVar, SQ_Const.textQuery, asSQNode() );
				
			}
			
		} else {
			
			if (hasLiteralRange) {

				throw new UnsupportedOperationException
					("@search on aspect " + (a == null ? aspectName : a) + " has @property " + property + " -- should have been detected earlier" );
				
			} else {
				
				return new SQ_Triple(aVar, SQ_Const.textQuery, asSQNode());
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

	@Override protected boolean constrains(Aspect a) {
		return aspectName == null ? false : aspectName.equals(a.getName());
	}	
}
