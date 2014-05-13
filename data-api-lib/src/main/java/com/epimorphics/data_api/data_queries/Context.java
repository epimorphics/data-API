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
import com.hp.hpl.jena.shared.PrefixMapping;

public class Context  {

	final Problems p;
	final DataQuery dq;
	final API_Dataset api;
	final StringBuilder out;
	
	
	final List<Aspect> ordered = new ArrayList<Aspect>();
	final Map<Shortname, Aspect> namesToAspects = new HashMap<Shortname, Aspect>();
	
	public Context( StringBuilder out, DataQuery dq, Problems p, API_Dataset api) {
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
	
	public Constraint begin(Constraint c) {
		comment("begin a SELECT query");
		generateSelect();
		out.append("WHERE {\n");
		return queryCore(c);
	}
	
	public void negateFilter(Filter negated) {
		String varSuffix = "_" + ++varCount;
		out
			.append( "  FILTER(NOT EXISTS {" )
			.append( " ?item" )
			.append( " " )
			.append( negated.a.asProperty() )
			.append( " " )
			.append( negated.a.asVar() + varSuffix )
			.append( " .\n")
			;
		negated.toSparql(this, varSuffix);
		out
			.append( "})" )
			.append( "\n" )
			;		
	}
	
	int varCount = 0;
	
	public void end() {
		comment("end a SELECT query");
		out.append("}").append("\n");
	}
	
	public void comment(String message, Object... values) {
		out.append( "# ").append(message);
		for (Object v: values) out.append(" ").append(v);
		out.append(".\n");
	}

	private void generateSelect() {		
		
		List<Guard> guards = dq.guards;
		boolean needsDistinct = false;
    	for (Guard guard : guards) if (guard.needsDistinct()) needsDistinct = true;
    	
    	out.append( "SELECT " );
		if (dq.isCountQuery()) {
		    for (Aspect as : api.getAspects()) {
		        if (as.getIsMultiValued()) {
		            needsDistinct = true;
		            break;
		        }
		    }
            out.append(" (COUNT (" + (needsDistinct ? "DISTINCT " : "") + "?item) AS ?_count)\n");
            if (dq.isNestedCountQuery()) {
            	comment("this is a nested count query, so:");
            	out.append("  { SELECT ?item").append("\n");
            }
		} else {
	        out.append( (needsDistinct ? "DISTINCT " : "") + "?item" );
	        for (Aspect x: ordered) out.append("\n  ").append( x.asVar() );
		}
		out.append("\n");
	}
	
	public Constraint queryCore(Constraint c) {
		List<Guard> guards = dq.guards;
        boolean baseQueryNeeded = true;  
        for (Guard guard : guards) {
            if (guard.supplantsBaseQuery()) {
                baseQueryNeeded = false;
            }
        }
    //
        String baseQuery = api.getBaseQuery();
		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded) {
			comment("base query");
		    out.append( "  ").append(baseQuery).append( "\n");
		} else {
			comment("no base query");
		}
	//
		int ng = guards.size();
        comment(ng == 0 ? "no guards" : ng == 1 ? "one guard" : ng + " guards");
        for (Guard guard : guards)
        	out.append(guard.queryFragment(api));
    // 
        return declareAspectVars(earlySearches(c));
	}

	private Constraint earlySearches(Constraint c) {
		if (c instanceof SearchSpec) {
        	generateSearch( (SearchSpec) c);
        	return Constraint.EMPTY;
        } else if (c instanceof And) {
        	List<Constraint> nonSearches = new ArrayList<Constraint>();
        	for (Constraint x: ((And) c).operands) {
        		if (x instanceof SearchSpec) {
        			generateSearch((SearchSpec) x);        			
        		} else {
        			nonSearches.add(x);
        		}
        	}
        	return Constraint.and(nonSearches);
        } else {
        	return c;
        }
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

	private Constraint declareAspectVars(Constraint c) {
		int nb = ordered.size();
		comment(nb == 0 ? "no aspect bindings": nb == 1 ? "one aspect binding" : nb + " aspect bindings");
	//
		Equalities equalities = new Equalities(p);
		Constraint adjusted = findEqualities(equalities, c);
		Set<Aspect> required = new HashSet<Aspect>();
		findRequiredAspects(required, c);
	//
		for (Aspect x: ordered) {
			String fVar = x.asVar();
			boolean isOptional = x.getIsOptional() && !required.contains(x);
			List<Term> allEquals = equalities.get(x.getName());
			if (allEquals.isEmpty()) {
				declareOneBinding(x, isOptional, 0, fVar, null);
			} else {
				PrefixMapping prefixes = api.getPrefixes();
				int countBindings = 0;
				for (Term equals: allEquals) {
					declareOneBinding(x, isOptional, countBindings, fVar, equals.asSparqlTerm(prefixes));
					countBindings += 1;
				}
			}
		}
		return adjusted;
	}

	private void declareOneBinding(Aspect x, boolean isOptional, int countBindings, String fVar, String stringEquals) {
		out.append("  ");
		if (isOptional) out.append( " OPTIONAL {" );
		out
			.append("?item")
			.append(" ").append(x.asProperty())
			.append(" ").append(stringEquals == null ? fVar : stringEquals)
			.append(" .")
			;
		if (isOptional) out.append( " }" );
		if (stringEquals != null && countBindings == 0) {
			out.append(" BIND(").append(stringEquals).append(" AS ").append(fVar).append(")");
		}
		out.append( "\n" );
	}
	
	private void findRequiredAspects(Set<Aspect> required, Constraint c) {
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
	private Constraint findEqualities(Equalities eq, Constraint c) {
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

	public void generateSearch(SearchSpec s) {
		comment("@search", s);
		out.append("  ");
		out.append(s.toSearchTriple(namesToAspects, api.getPrefixes()));
		out.append(" .\n");
	}

	public void nest() {
		comment("nest for @or");
		out.append("{{\n");
	}

	public void unNest() {
		comment("un-nest for @or");
		out.append("}}\n");
	}

	public void union() {
		out.append( "}} UNION {{\n" );
	}
}