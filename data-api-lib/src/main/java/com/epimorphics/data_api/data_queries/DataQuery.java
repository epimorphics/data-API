/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.Composition.And;
import com.epimorphics.data_api.data_queries.Composition.Context;
import com.epimorphics.data_api.data_queries.Composition.FilterWrap;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.shared.PrefixMapping;

public class DataQuery {
	
	private static final Comparator<? super Aspect> compareAspects = new Comparator<Aspect>() {

		@Override public int compare(Aspect a, Aspect b) {
			if (a.getIsOptional() == b.getIsOptional()) return a.getID().compareTo(b.getID());
			return a.getIsOptional() ? +1 : -1;
		}
	};
	
	final List<Sort> sortby;
	final Slice slice;
	final List<Guard> guards; 
	
	final Composition c;
	
	final List<SearchSpec> searchPatterns = new ArrayList<SearchSpec>();
	
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
    	this(c, sortby, guards, slice, SearchSpec.none());
    }

    public DataQuery(Composition c, List<Sort> sortby, List<Guard> guards, Slice slice, List<SearchSpec> searchPatterns) {
		this.c = c;
		this.sortby = sortby;
		this.slice = slice;
		this.guards = guards == null ? new ArrayList<Guard>(0) : guards;
		this.searchPatterns.addAll(searchPatterns);
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

	public List<SearchSpec> getSearchPatterns() {
		return searchPatterns;
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
	    
	private String toSparqlString(Problems p, Set<Aspect> aspects, String baseQuery, PrefixMapping pm, API_Dataset api) {
		return newWay(p, aspects, baseQuery, pm, api);
	}

	private String newWay(Problems p, Set<Aspect> aspects, String baseQuery, PrefixMapping pm, API_Dataset api) {

		List<Aspect> ordered = new ArrayList<Aspect>(aspects);
		Collections.sort(ordered, compareAspects);
		
		StringBuilder sb = new StringBuilder();
				
		ContextImpl cx = new ContextImpl(this, sb, p, aspects, ordered, baseQuery, pm, api);
		
		if (c.isPure()) {
			cx.generateHead();
			cx.BEGIN();
			if (c.isTrivial()) {
				cx.footPrint("trivial", this);
			} else {
				cx.generate(" FILTER(" );
				c.asFilter(cx);
				cx.generate(")");
			}
			cx.END();
		} else {			
			c.topLevel(cx);
		}

		querySort(sb);
		if (slice.length != null) sb.append( " LIMIT " ).append(slice.length);
		if (slice.offset != null) sb.append( " OFFSET " ).append(slice.offset);
		
		return PrefixUtils.expandQuery(sb.toString(), pm);
	}
	
	static class ContextImpl implements Context {
		
		final DataQuery dq;
		final StringBuilder sb;
		final Problems p;
		final Set<Aspect> aspects;
		final List<Aspect> ordered;
		final String baseQuery;
		final PrefixMapping pm;
		final API_Dataset api;
		
		public ContextImpl
			( DataQuery dq
			, StringBuilder sb
			, Problems p
			, Set<Aspect> aspects
			, List<Aspect> ordered
			, String baseQuery
			, PrefixMapping pm
			, API_Dataset api
		) {
			this.dq = dq;
			this.sb = sb;
			this.p = p;
			this.aspects = aspects;
			this.ordered = ordered;
			this.baseQuery = baseQuery;
			this.pm = pm;
			this.api = api;
		}

		@Override public void footPrint(String message, Object value) {
			sb.append( " # ")
				.append(message)
				.append(" ")
				.append(value)
				.append('\n')
				;
		}

		@Override public void generateHead() {
			String head = dq.queryHead(p, aspects, baseQuery, pm, api);
			sb.append(head);
		}

		@Override public void BEGIN() {
			String core = dq.queryCore(p, aspects, baseQuery, pm, api);
			sb.append(" {\n" );
			sb.append(core);
		}

		@Override public void END() {
			sb.append(" }\n" );
		}

		@Override public void FILTER(Filter f, boolean sayFILTER) {
			f.range.op.asSparqlFilter
				( pm
				, f
				, sb
				, (sayFILTER ? "FILTER" : "")
				, api
				, ordered
				, "?" + f.name.asVar()
				, f.range.operands.get(0).asSparqlTerm(pm)
				);
		}

		@Override public void generate(String fragment) {
			sb.append(fragment);
		}
	}

	private void querySort(StringBuilder sb) {
		if (sortby.size() > 0) {
			sb.append(" ORDER BY");
			for (Sort s: sortby) {
				sb.append(" ");
				if (!s.upwards) sb.append("DESC(");
				sb.append( "?" ).append(s.by.asVar());
				if (!s.upwards)sb.append(")"); 
			}
		}
	}


	private String queryHead(Problems p, Set<Aspect> a,	String baseQuery, PrefixMapping pm, API_Dataset api) {
		List<Aspect> ordered = new ArrayList<Aspect>(a);
		Collections.sort(ordered, compareAspects);
	//	
		boolean needsDistinct = false;
        for (Guard guard : guards) if (guard.needsDistinct()) needsDistinct = true;
    //
        StringBuilder sb = new StringBuilder();
		sb.append( "\nSELECT " + (needsDistinct ? "DISTINCT" : "") + "?item");
		for (Aspect x: ordered) sb.append(" ?").append( x.asVar() );		
		return sb.toString();
	}

	private String queryCore(Problems p, Set<Aspect> a,	String baseQuery, PrefixMapping pm, API_Dataset api) {
		List<Aspect> ordered = new ArrayList<Aspect>(a);
		Collections.sort(ordered, compareAspects);
	
		Map<Shortname, Aspect> namesToAspects = new HashMap<Shortname, Aspect>();
		for (Aspect x: a) namesToAspects.put(x.getName(), x);
		
		StringBuilder sb = new StringBuilder();
        boolean baseQueryNeeded = true;
        
        for (Guard guard : guards) {
            if (guard.supplantsBaseQuery()) {
                baseQueryNeeded = false;
            }
        }
		String dot = "";
	//
		for (SearchSpec s: searchPatterns) {
            sb.append(dot).append(s.toSearchTriple(namesToAspects, pm));
            dot = " .\n ";
        }
	//
		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded) {
		    // sb.append("# BASE QUERY\n");
		    sb.append(dot).append("{ ").append( baseQuery ).append(" }");
//            dot = " .\n";
            dot = "\n";
		}
	    for (Guard guard : guards) {
	        sb.append(dot);
	        dot = "\n";
	        sb.append(guard.queryFragment(api));
	    }
	    establishAspectVars(dot, pm, sb, ordered);
	    return sb.toString();
	}

	private String establishAspectVars(String dot, PrefixMapping pm, StringBuilder sb, List<Aspect> ordered) {
		for (Aspect x: ordered) {
			String fVar = "?" + x.asVar();
			sb.append(dot);	dot = "\n.";
		//
			String eqValue = findEqualityValue(pm, x.getName(), c);			
			boolean isEquality = eqValue != null;				
		//		
			if (x.getIsOptional()) sb.append( " OPTIONAL {" );
			sb
				.append(" ")
				.append("?item")
				.append(" ").append(x.asProperty())
				.append(" ").append(isEquality ? eqValue : fVar)
				;
			if (x.getIsOptional()) sb.append( " }" );
			if (isEquality) {
				sb.append(" BIND(").append(eqValue).append(" AS ").append(fVar).append(")");
			}
		}
		return dot;
	}

	private String findEqualityValue(PrefixMapping pm, Shortname name, Composition c) {
		if (c instanceof FilterWrap) {
			Filter f = ((FilterWrap) c).f;
			if (f.name.prefixed.equals(name.prefixed)) {
				if (f.range.op.equals(Operator.EQ)) 
					return f.range.operands.get(0).asSparqlTerm(pm);
			}
			return null;
		} else if (c instanceof And) {
			String eqValue = null;
			
			for (Composition x: c.operands) {
				String eq = findEqualityValue(pm, name, x);
				if (eqValue == null) {
					eqValue = eq;
				} else if (eq == null) {
					// nothing to do
				} else if (eq.equals(eqValue)) {
					// also nothing to do
				} else {
					// conflicting values
					return null;
				}
			}
			
			return eqValue;
		} else {
			return null;
		}
	}
}