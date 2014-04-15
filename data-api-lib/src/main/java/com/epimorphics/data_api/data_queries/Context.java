/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Constraint.And;
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

	private Constraint declareAspectVars(Constraint c) {
		int nb = ordered.size();
		comment(nb == 0 ? "no aspect bindings": nb == 1 ? "one aspect binding" : nb + " aspect bindings");
	//
		Map<Shortname, Term> equalities = new HashMap<Shortname, Term>();
		Constraint adjusted = findEqualities(equalities, c);
	//
		for (Aspect x: ordered) {
			String fVar = x.asVar();
			Term equals = equalities.get(x.getName());
			String stringEquals = equals == null ? null : equals.asSparqlTerm(api.getPrefixes());
		//
			out.append("  ");
		//
			if (x.getIsOptional()) out.append( " OPTIONAL {" );
			out
				.append("?item")
				.append(" ").append(x.asProperty())
				.append(" ").append(stringEquals == null ? fVar : stringEquals)
				.append(" .")
				;
			if (x.getIsOptional()) out.append( " }" );
			if (stringEquals != null) {
				out.append(" BIND(").append(stringEquals).append(" AS ").append(fVar).append(")");
			}
			out.append( "\n" );
		}
		return adjusted;
	}
	
	private Constraint findEqualities(Map<Shortname, Term> result, Constraint c) {
		if (c instanceof Filter) {
			Filter f = ((Filter) c);
			if (f.range.op.equals(Operator.EQ)) {
				result.put(f.a.getName(), f.range.operands.get(0));
				return Constraint.EMPTY;
			} else {
				return c;
			}
		} else if (c instanceof And) {
			List<Constraint> operands = new ArrayList<Constraint>();
			for (Constraint x: ((And) c).operands) operands.add( findEqualities(result, x) );
			return Constraint.and(operands);
		} else {
			return c;
		}
	}

	public void notImplemented(Constraint c) {
		System.err.println( ">> not implemented: " + c );
		comment("not implemented: " + c.toString());
	}

	public void generateBelow(Below b) {
		
		comment("@below", b);
//			f.range.op.asConstraint( f, out, api );
//			out.append("\n");	
		
		PrefixMapping pm = api.getPrefixes();
		String fVar = b.a.asVar(); 
		String value = b.v.asSparqlTerm(pm);			
		Aspect x = b.a;
		String below = x.getBelowPredicate(api);
		out.append(value)
			.append(" ")
			.append(below)
			.append("* ")
			.append(fVar)
			.append(" .")
			.append("\n")
			;
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