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
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.conversions.CountWriter;
import com.epimorphics.data_api.conversions.RowWriter;
import com.epimorphics.data_api.data_queries.Composition.And;
import com.epimorphics.data_api.data_queries.Composition.FilterWrap;
import com.epimorphics.data_api.data_queries.Composition.Context;
import com.epimorphics.data_api.data_queries.Composition.SearchWrap;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.shared.PrefixMapping;

public class DataQuery {
	
	final List<Sort> sortby;
	final Slice slice;
	final List<Guard> guards; 
	
	final Composition c;
	
	public DataQuery(Composition c) {
		this(c, new ArrayList<Sort>() );
	}

	public DataQuery(Composition c, List<Sort> sortby ) {
        this(c, sortby, null, Slice.all());
    }

    public DataQuery(Composition c, List<Sort> sortby, List<Guard> guards ) {
        this(c, sortby, guards, Slice.all());
    }
    
    public DataQuery(Composition c, List<Sort> sortby, Slice slice) {
        this(c, sortby, null, slice);
    }    
    
    public DataQuery(Composition c, List<Sort> sortby, List<Guard> guards, Slice slice) {
		this.c = c;
		this.sortby = sortby;
		this.slice = slice;
		this.guards = guards == null ? new ArrayList<Guard>(0) : guards;
	}

	public List<Sort> sorts() {
		return sortby;
	}
	
	public String lang() {
		return null;
	}
	
	public Slice slice() {
		return slice;
	}
    
    public boolean isCountQuery() {
        return slice.isCount;
    }
    
    public boolean isNestedCountQuery() {
        return slice.isCount && (slice.length != null || slice.offset != null);
    }

	public List<SearchSpec> getSearchPatterns() {
		// TODO revise so it's not hackery
		// this getter is only used for tests
		// so tempoarilty faking it out is OK
		List<SearchSpec> result = new ArrayList<SearchSpec>();
		hackery(result, c);
		return result;
	}
	
	private void hackery(List<SearchSpec> result, Composition c) {
		if (c instanceof SearchWrap) {
			result.add(((SearchWrap) c).s);
		} else {
			for (Composition x: c.operands) hackery(result, x);
		}
	}

	public List<Filter> filters() {
		ArrayList<Filter> result = new ArrayList<Filter>();
		if (c instanceof And) 
			for (Composition cc: c.operands)
				if (cc instanceof FilterWrap) result.add( ((FilterWrap) cc).f );
		if (c instanceof FilterWrap) result.add( ((FilterWrap) c).f );
		return result;
	}
    
    public String toSparql(Problems p, API_Dataset api) {
        try { return toSparqlString(p, api.getAspects(), api.getBaseQuery(), api.getPrefixes(), api); }
        catch (Exception e) { p.add("exception generating SPARQL query: " + e.getMessage()); e.printStackTrace(System.err); return null; }
    }
    
    public String toSparql(Problems p, Aspects a, String baseQuery, PrefixMapping pm) {
        try { return toSparqlString(p, a.getAspects(), baseQuery, pm, null); }
        catch (Exception e) { p.add("exception generating SPARQL query: " + e.getMessage()); e.printStackTrace(System.err); return null; }
    }
	    
    @Override public String toString() {
    	return 
    		c
    		+ (sortby.isEmpty() ? "" : "\n    sortby: " + sortby)
    		+ (!slice.isAll() ? ""   : "\n    slice:  " + slice)
    		+ (guards.isEmpty() ? "" : "\n    guards: " + guards)
    		+ "\n"
    		;
    }
    
    public JSONWritable getWriter(API_Dataset api, ResultSet resultSet) {
    	if (isCountQuery()) {
    		return new CountWriter(resultSet);
    	} else {
    		return new RowWriter(api.getAspects(), resultSet);
    	}
    }
    
	private String toSparqlString(Problems p, Set<Aspect> aspects, String baseQuery, PrefixMapping pm, API_Dataset api) {
				
//		System.err.println( ">> " );
//		System.err.println( ">> toSparql: " + c );
//		System.err.println( ">>   isPure: " + c.isPure() + ", isTrivial: " + c.isTrivial() );
//		System.err.println( ">> FOR:\n    " + this );
//		System.err.println( ">> ASPECTS:\n    " + aspects );

		StringBuilder out = new StringBuilder();
		ContextImpl rx = new ContextImpl
			( out
			, this
			, p
			, aspects
			, baseQuery
			, pm
			, api
			, guards
			);
		
		Composition adjusted = rx.begin(c);
		adjusted.toSparql(rx);
		rx.end();
		
		querySort(out);		
		if (slice.length != null) out.append( " LIMIT " ).append(slice.length);
		if (slice.offset != null) out.append( " OFFSET " ).append(slice.offset);
		
		if (isNestedCountQuery()) out.append("}");
		
		String query = PrefixUtils.expandQuery(out.toString(), pm);
		
		// System.err.println( ">> RENDERED QUERY:\n" + query );
		return query;
	}
	
	static class ContextImpl implements Context {

		final StringBuilder out;
		final Problems p;
		final Set<Aspect> aspects;
		final String baseQuery;
		final PrefixMapping pm;
		final API_Dataset api;
		final List<Guard> guards;
		final DataQuery dq;
		
		final List<Aspect> ordered = new ArrayList<Aspect>();
		final Map<Shortname, Aspect> namesToAspects = new HashMap<Shortname, Aspect>();
		
		public ContextImpl
			( StringBuilder out
			, DataQuery dq
			, Problems p
			, Set<Aspect> aspects
			, String baseQuery
			, PrefixMapping pm
			, API_Dataset api
			, List<Guard> guards
			) {
			this.out = out;		
			this.dq = dq;
			this.p = p;
			this.aspects = aspects;
			this.baseQuery = baseQuery;
			this.pm = pm;
			this.api = api;
			this.guards = guards;
		//
			this.ordered.addAll(aspects);
			Collections.sort(this.ordered, Aspect.compareAspects);
		//
			for (Aspect x: aspects) namesToAspects.put(x.getName(), x);
		}
		
		public Composition begin(Composition c) {
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
			
			boolean needsDistinct = false;
        	for (Guard guard : guards) if (guard.needsDistinct()) needsDistinct = true;
        	
        	out.append( "SELECT " );
    		if (dq.isCountQuery()) {
    		    for (Aspect as : aspects) {
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
    	        for (Aspect x: ordered) out.append(" ").append( x.asVar() );
    		}
			out.append("\n");
		}
		
		public Composition queryCore(Composition c) {
	        boolean baseQueryNeeded = true;  
	        for (Guard guard : guards) {
	            if (guard.supplantsBaseQuery()) {
	                baseQueryNeeded = false;
	            }
	        }
	    //
			if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded) {
				comment("base query");
			    out.append( "  { ").append(baseQuery).append( "}\n");
			} else {
				comment("no base query");
			}
		//
			int ng = guards.size();
	        comment(ng == 0 ? "no guards" : ng == 1 ? "one guard" : ng + " guards");
	        for (Guard guard : guards)
	        	out.append(guard.queryFragment(api));
	    //
	        return declareAspectVars(c);
		}

		private Composition declareAspectVars(Composition c) {
			int nb = ordered.size();
			comment(nb == 0 ? "no aspect bindings": nb == 1 ? "one aspect binding" : nb + " aspect bindings");
		//
			Map<Shortname, Term> equalities = new HashMap<Shortname, Term>();
			Composition adjusted = findEqualities(equalities, c);
		//
			for (Aspect x: ordered) {
				String fVar = x.asVar();
				Term equals = equalities.get(x.getName());
				String stringEquals = equals == null ? null : equals.asSparqlTerm(pm);
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
		
		private Composition findEqualities(Map<Shortname, Term> result, Composition c) {
			if (c instanceof FilterWrap) {
				Filter f = ((FilterWrap) c).f;
				if (f.range.op.equals(Operator.EQ)) {
					result.put(f.name, f.range.operands.get(0));
					return Composition.EMPTY;
				} else {
					return c;
				}
			} else if (c instanceof And) {
				List<Composition> operands = new ArrayList<Composition>();
				for (Composition x: c.operands) operands.add( findEqualities(result, x) );
				return Composition.and(operands);
			} else {
				return c;
			}
		}

		@Override public void notImplemented(Composition c) {
			System.err.println( ">> not implemented: " + c );
			comment("not implemented: " + c.toString());
		}

		@Override public void generateFilter(Filter f) {
			comment("@" + f.range.op.JSONname, f);
			out.append("  ");
			f.range.op.asSparqlFilter
				( pm
				, f
				, out
				, "FILTER"
				, api
				, ordered
				);
			out.append("\n");
		}

		@Override public void generateSearch(SearchSpec s) {
			comment("@search", s);
			out.append("  ");
			out.append(s.toSearchTriple(namesToAspects, pm));
			out.append(" .\n");
		}

		@Override public void nest() {
			comment("nest for @or");
			out.append("{{\n");
		}

		@Override public void unNest() {
			comment("un-nest for @or");
			out.append("}}\n");
		}

		@Override public void union() {
			out.append( "}} UNION {{\n" );
		}
		
	}

	private void querySort(StringBuilder sb) {
		if (sortby.size() > 0) {
			sb.append(" ORDER BY");
			for (Sort s: sortby) {
				sb.append(" ");
				if (!s.upwards) sb.append("DESC(");
				sb.append(s.by.asVar());
				if (!s.upwards)sb.append(")"); 
			}
		}
	}
}