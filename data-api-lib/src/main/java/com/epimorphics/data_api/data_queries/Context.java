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
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ.Const;
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
		Collections.sort(this.ordered, Aspect.compareAspects);
	//
		for (Aspect x: aspects) namesToAspects.put(x.getName(), x);
	}
	
	public void negateFilter(Filter negated) {
//		String varSuffix = "_" + ++varCount;
//		out
//			.append( "  FILTER(NOT EXISTS {" )
//			.append( " ?item" )
//			.append( " " )
//			.append( negated.a.asProperty() )
//			.append( " " )
//			.append( negated.a.asVar() + varSuffix )
//			.append( " .\n")
//			;
//		negated.toSparql(this, varSuffix);
//		out
//			.append( "})" )
//			.append( "\n" )
//			;		
		throw new RuntimeException("TBD");
	}
	
	int varCount = 0;
	
	public void comment(String message, Object... values) {
		out.append( "# ").append(message);
		for (Object v: values) out.append(" ").append(v);
		out.append(".\n");
	}

//	private void generateSelect() {		
//		
//		List<Guard> guards = dq.guards;
//		boolean needsDistinct = false;
//    	for (Guard guard : guards) if (guard.needsDistinct()) needsDistinct = true;
//    	
//    	out.append( "SELECT " );
//		if (dq.isCountQuery()) {
//		    for (Aspect as : api.getAspects()) {
//		        if (as.getIsMultiValued()) {
//		            needsDistinct = true;
//		            break;
//		        }
//		    }
//            out.append(" (COUNT (" + (needsDistinct ? "DISTINCT " : "") + "?item) AS ?_count)\n");
//            if (dq.isNestedCountQuery()) {
//            	comment("this is a nested count query, so:");
//            	out.append("  { SELECT ?item").append("\n");
//            }
//		} else {
//	        out.append( (needsDistinct ? "DISTINCT " : "") + "?item" );
//	        for (Aspect x: ordered) out.append("\n  ").append( x.asVar() );
//		}
//		out.append("\n");
//	}
	
//	public Constraint queryCore(Constraint c) {
//		List<Guard> guards = dq.guards;
//        boolean baseQueryNeeded = true;  
//        for (Guard guard : guards) {
//            if (guard.supplantsBaseQuery()) {
//                baseQueryNeeded = false;
//            }
//        }
//    //
//        String baseQuery = api.getBaseQuery();
//		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded) {
//			comment("base query");
//		    out.append( "  ").append(baseQuery).append( "\n");
//		} else {
//			comment("no base query");
//		}
//	//
//		int ng = guards.size();
//        comment(ng == 0 ? "no guards" : ng == 1 ? "one guard" : ng + " guards");
//        for (Guard guard : guards)
//        	out.append(guard.queryFragment(api));
//    // 
//        return declareAspectVars(earlySearches(c));
//	}
//
//	public Constraint earlySearches(Constraint c) {
//		if (isItemSearch(c)) {
//        	generateSearch( (SearchSpec) c);
//        	return Constraint.EMPTY;
//        } else if (c instanceof And) {
//        	List<Constraint> nonSearches = new ArrayList<Constraint>();
//        	for (Constraint x: ((And) c).operands) {
//        		if (isItemSearch(x)) {
//        			generateSearch((SearchSpec) x);        			
//        		} else {
//        			nonSearches.add(x);
//        		}
//        	}
//        	return Constraint.and(nonSearches);
//        } else {
//        	return c;
//        }
//	}

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
			SQ.Variable var = new SQ.Variable(fVar.substring(1));
			boolean isOptional = x.getIsOptional() && !required.contains(x);
			List<Term> allEquals = equalities.get(x.getName());
			if (allEquals.isEmpty()) {
				declareOneBindingSQ(x, isOptional, 0, var, null);
			} else {
				PrefixMapping prefixes = api.getPrefixes();
				int countBindings = 0;
				for (Term equals: allEquals) {
					declareOneBindingSQ(x, isOptional, countBindings, var, equals);
					countBindings += 1;
				}
			}
		}
		return adjusted;
	}

	private void declareOneBindingSQ(Aspect x, boolean isOptional, int countBindings, SQ.Variable var, Term equalTo) {		
		SQ.Resource property = new SQ.Resource(x.asProperty());
		
		SQ.Triple t = new SQ.Triple(Const.item, property, (equalTo == null ? var : termAsNode(equalTo)) );
			
		if (isOptional) sq.addOptionalTriple(t); else sq.addTriple(t);
		
		if (equalTo != null && countBindings == 0) {
			sq.addBind(Range.termAsExpr(equalTo), var);		
		}
	}
	
	private SQ.Node termAsNode(final Term equalTo) {		
		final PrefixMapping pm = PrefixMapping.Factory.create();

		return new SQ.Node() {

			@Override public void toString(StringBuilder sb) {
				sb.append(equalTo.asSparqlTerm(pm));

			}};
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