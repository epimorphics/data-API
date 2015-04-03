/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.*;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.*;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Context  {

	final SQ sq;
	final Problems p;
	final DataQuery dq;
	final API_Dataset api;
	final StringBuilder out;
	
	final List<Aspect> ordered = new ArrayList<Aspect>();
	
	public Context( SQ sq, StringBuilder out, DataQuery dq, Problems p, API_Dataset api) {
		this.sq = sq;
		this.out = out;		
		this.dq = dq;
		this.p = p;
		this.api = api;
	//
		Set<Aspect> aspects = api.getAspects();
		this.ordered.addAll(aspects);
	//
		Shortname searchProperty = getSearchProperty();
	//
		Set<Aspect> constrained = new HashSet<Aspect>();
		for (Aspect a: ordered)
			if (dq.constraint().constrains(a))
				constrained.add(a);
	//
		Collections.sort(this.ordered, Aspect.compareConstrainedAspects(searchProperty, constrained));	
	}

	private Shortname getSearchProperty() {
		return getSearchProperty(dq.constraint());
	}

	private Shortname getSearchProperty(Constraint c) {
		if (c instanceof And) {
			for (Constraint x: ((And) c).operands) {
				Shortname found = getSearchProperty(x);
				if (found != null) return found;
			}
			return null;
		} else if (c instanceof SearchSpec) {
			return ((SearchSpec) c).getAspectName();
		} else {
			return null;
		}
	}

	public void declareOneBindingSQ(Aspect x, boolean isOptional, int countBindings, SQ_Variable var, Term equalTo) {		
						
		String rawProperty = x.asProperty();		
		List<SQ_Triple> triples = new ArrayList<SQ_Triple>(); 
		
		SQ_Node currentVariable = SQ_Const.item;
		
		String[] elements = rawProperty.split("/");
		int remainingElements = elements.length;
		boolean firstElement = true;
		
		if (elements.length > 1) 
			sq.comment("dealing with property path " + rawProperty + " for aspect " + x);
		
		// sq.comment("declaring binding for", x, "property", rawProperty);
		
		for (String element: elements) {
			remainingElements -= 1;
			
			// sq.comment("element", element);

			SQ_Resource currentPredicate = new SQ_Resource(element);
			
			String thisElementName = Shortname.asVarName(element);
			String nextVariableName = firstElement 
				? thisElementName
				: ((SQ_Variable) currentVariable).name() + "__" + thisElementName
				;
			
			SQ_Node nextObject = remainingElements == 0 
				? (equalTo == null ? var : termAsNode(equalTo))
				: new SQ_Variable(nextVariableName)
				;
			
			triples.add(new SQ_Triple(currentVariable, currentPredicate, nextObject));
			
			currentVariable = nextObject;
			
			firstElement = false;
		}
				
		if (isOptional) sq.addOptionalTriples(triples); else sq.addTriples(triples);
		
		if (equalTo != null && countBindings == 0) {
			// sq.addBind(Range.termAsExpr(api.getPrefixes(), equalTo), var);		
			sq.addBind(termAsNode(equalTo), var);
		}		
	}
	
	private SQ_Node termAsNode(final Term equalTo) {		
		final PrefixMapping pm = api.getPrefixes();
		return new SQ_TermAsNode(pm, equalTo);
	}
	
	public void generateSearchSQ(SearchSpec s) {
		s.toSearchTripleSQ(this);
	}
}