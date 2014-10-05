/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.*;

import com.epimorphics.data_api.Switches;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.SQ_Const;
import com.epimorphics.data_api.sparql.SQ_Node;
import com.epimorphics.data_api.sparql.SQ_Resource;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ_TermAsNode;
import com.epimorphics.data_api.sparql.SQ_Triple;
import com.epimorphics.data_api.sparql.SQ_Variable;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Context  {

	final SQ sq;
	final Problems p;
	final DataQuery dq;
	final API_Dataset api;
	final StringBuilder out;
	
	final List<Aspect> ordered = new ArrayList<Aspect>();
	final Map<Shortname, Aspect> namesToAspects = new HashMap<Shortname, Aspect>();
	
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
		
//		System.err.println(">> constrained aspects:");
//		for (Aspect a: constrained) System.err.println(">>  " + a);
//		System.err.println(">> ordered aspects:");
//		for (Aspect a: ordered) System.err.println(">>  " + a);
				
	//
		for (Aspect x: aspects) namesToAspects.put(x.getName(), x);
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

	public Constraint earlySearchesSQ(Constraint c) {
		if (isItemSearch(c)) {
        	generateSearchSQ( (SearchSpec) c);
        	return Constraint.EMPTY;
        } else if (c instanceof And) {
        	List<Constraint> nonSearches = new ArrayList<Constraint>();
        	for (Constraint x: ((And) c).operands) {
        		if (isItemSearch(x)) {
        			generateSearchSQ((SearchSpec) x);        			
        		} else {
        			nonSearches.add(x);
        		}
        	}
        	return Constraint.and(nonSearches);
        } else {
        	return c;
        }
	}
	
	public boolean isItemSearch(Constraint c) {
		if (c instanceof SearchSpec) {
			SearchSpec s = (SearchSpec) c;
			Aspect a = namesToAspects.get(s.getAspectName());
			if (s.hasLiteralRange(a)) return true;
		}
		return false;
	}
	
	public static class Equalities  {
		
		private Map<Shortname, List<Term>> map = new HashMap<Shortname, List<Term>>();

		final Problems p;
		
		public Equalities(Problems p) {
			this.p = p;
		}
		
		public void put(Aspect a, Shortname name, Term value) {
			List<Term> terms = map.get(name);
			if (terms == null) {
				map.put(name,  terms = new ArrayList<Term>());
			} else {
				// TODO p.add("Warning: multiple values given for equality on aspect " + a);
			}
			terms.add(value);
		}
		
		static final List<Term> NO_TERMS = new ArrayList<Term>();

		public List<Term> get(Shortname name) {
			List<Term> terms = map.get(name);
			return terms == null ? NO_TERMS : terms;
		}		
	}

	public Constraint declareAspectVarsSQ(Constraint c) {
		int nb = ordered.size();
		// comment(nb == 0 ? "no aspect bindings": nb == 1 ? "one aspect binding" : nb + " aspect bindings");
	//
		Equalities equalities = new Equalities(p);
		Constraint adjusted = findEqualities(equalities, c);
		Set<Aspect> required = new HashSet<Aspect>();
		findRequiredAspects(required, c);
	//
		for (Aspect x: ordered) {
			String fVar = x.asVar();
			SQ_Variable var = new SQ_Variable(fVar.substring(1));
			boolean isOptional = x.getIsOptional() && !required.contains(x);
			List<Term> allEquals = equalities.get(x.getName());
			if (allEquals.isEmpty()) {
				declareOneBindingSQ(x, isOptional, 0, var, null);
			} else {
				int countBindings = 0;
				for (Term equals: allEquals) {
					declareOneBindingSQ(x, isOptional, countBindings, var, equals);
					countBindings += 1;
				}
			}
		}
		return adjusted;
	}

	private void declareOneBindingSQ(Aspect x, boolean isOptional, int countBindings, SQ_Variable var, Term equalTo) {		
		
		if (Switches.onlyImplicityPropertyPathsWay) {
			SQ_Resource property = new SQ_Resource(x.asProperty());
			
			SQ_Triple t = new SQ_Triple(SQ_Const.item, property, (equalTo == null ? var : termAsNode(equalTo)) );
				
			if (isOptional) {
				sq.addOptionalTriples(BunchLib.list(t)); 
			} else {
				sq.addTriple(t);
			}
			
			if (equalTo != null && countBindings == 0) {
				sq.addBind(Range.termAsExpr(api.getPrefixes(), equalTo), var);		
			}
			return;
		}
		
		// System.err.println(">> declareOneBinding, for " + x + (isOptional ? " (optional)" : ""));
		
		String rawProperty = x.asProperty();
		// SQ_Resource property = new SQ_Resource(rawProperty);
		
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
			sq.addBind(Range.termAsExpr(api.getPrefixes(), equalTo), var);		
		}		
	}
	
	private SQ_Node termAsNode(final Term equalTo) {		
		final PrefixMapping pm = api.getPrefixes();
		return new SQ_TermAsNode(pm, equalTo);
	}

	public void findRequiredAspects(Set<Aspect> required, Constraint c) {
		if (c instanceof Filter) {
			required.add( ((Filter) c).a );
		} else if (c instanceof And) {
			for (Constraint x: ((And) c).operands) findRequiredAspects(required, x);
		} else {
			// TODO consider if there are other cases to go here
		}
	}

	/**
	    Explore the @and operands of the constraint c looking for
	    aspects equal to some value. Return the tree with such aspects
	    stripped out and put into the Equalities table. 
	*/
	public Constraint findEqualities(Equalities eq, Constraint c) {
		if (c instanceof Filter) {
			Filter f = ((Filter) c);
			if (f.range.op.equals(Operator.EQ)) {	
				
				Term value = f.range.operands.get(0);
				eq.put(f.a, f.a.getName(), value);				
				
				return Constraint.EMPTY;
			} else {
				return c;
			}
		} else if (c instanceof And) {
			List<Constraint> operands = new ArrayList<Constraint>();
			for (Constraint x: ((And) c).operands) operands.add( findEqualities(eq, x) );
			return Constraint.and(operands);
		} else {
			return c;
		}
	}

	public void generateSearchSQ(SearchSpec s) {
		s.toSearchTripleSQ(this, namesToAspects, api.getPrefixes());
	}
}