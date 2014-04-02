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
import com.epimorphics.data_api.data_queries.Composition.And;
import com.epimorphics.data_api.data_queries.Composition.Context;
import com.epimorphics.data_api.data_queries.Composition.FilterWrap;
import com.epimorphics.data_api.data_queries.Composition.RenderContext;
import com.epimorphics.data_api.data_queries.Composition.SearchWrap;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.util.PrefixUtils;
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
    
	private String toSparqlString(Problems p, Set<Aspect> aspects, String baseQuery, PrefixMapping pm, API_Dataset api) {
				
		boolean impure = !c.isPure();
		ContextImpl cx = new ContextImpl(this, p, aspects, baseQuery, pm, api, impure);
		
		System.err.println( ">> toSparql: " + c );
//		System.err.println( ">>   isPure: " + c.isPure() + ", isTrivial: " + c.isTrivial() );
//		System.err.println( ">> FOR:\n    " + this );
//		System.err.println( ">> ASPECTS:\n    " + aspects );
		
//		if (baseQuery != null && baseQuery.length() > 0)	
//			System.err.println( ">> BASE QUERY:\n    " + baseQuery);
		
		if (c.isPure()) {
			cx.addQueryHead();
			cx.addQueryCore();
			if (c.isTrivial()) {
				cx.comment("trivial", c);
			} else {
				cx.block.setPureFilter(c);
			}
		} else {			
			c.topLevel(cx);
		}
		
		StringBuilder out = new StringBuilder();
		RenderContextImpl rx = new RenderContextImpl
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
		Composition.render(adjusted, rx);
		rx.end();
		String newerCode = sortAndSlice(pm, out);
		System.err.println( ">> RENDERED QUERY:\n" + newerCode );
		
		StringBuilder sb = new StringBuilder();		
		cx.block.toSparql(sb);
		
		String newCode = sortAndSlice(pm, sb);
		
//		System.err.println( ">> Generated SPARQL query:\n" + newCode);
//		return newCode;
		 return newerCode;
	}

	private String sortAndSlice(PrefixMapping pm, StringBuilder sb) {
		querySort(sb);
		
		if (slice.length != null) sb.append( " LIMIT " ).append(slice.length);
		if (slice.offset != null) sb.append( " OFFSET " ).append(slice.offset);
		
		String newCode = PrefixUtils.expandQuery(sb.toString(), pm);
		return newCode;
	}
	
	static class RenderContextImpl implements RenderContext {

		final StringBuilder out;

		final DataQuery dq;
		final Problems p;
		final Set<Aspect> aspects;
		final String baseQuery;
		final PrefixMapping pm;
		final API_Dataset api;
		final List<Guard> guards;
		
		final List<Aspect> ordered = new ArrayList<Aspect>();
		final Map<Shortname, Aspect> namesToAspects = new HashMap<Shortname, Aspect>();
		
		public RenderContextImpl
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
		
        	out.append("\n")
				.append("SELECT ")
				.append(needsDistinct ? "DISTINCT " : "")
				.append(" ")
				.append("?item")
				;
			for (Aspect x: ordered) out.append("\n     ?").append( x.asVar() );
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
				String fVar = "?" + x.asVar();
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
				, "?" + f.name.asVar()
				, f.range.operands.get(0).asSparqlTerm(pm)
				);
			out.append("\n");
		}

		@Override public void generateSearch(SearchSpec s) {
			comment("@search", s);
//			out.append("  { ");
			out.append("  ");
			out.append(s.toSearchTriple(namesToAspects, pm));
			out.append(" .\n");
//			out.append(" }");
		}
		
	}
	
	static abstract class PreSPARQL {
		public abstract void toSparql(StringBuilder sb);
	}
	
	static class PreSPARQL_Filter extends PreSPARQL {
		
		final Filter f;
		final boolean notNested;
		
		final PrefixMapping pm;
		final API_Dataset api;
		final List<Aspect> ordered;
		
		public PreSPARQL_Filter(Filter f, boolean notNested, PrefixMapping pm, API_Dataset api, List<Aspect> ordered) {
			this.f = f;
			this.notNested = notNested;
		//
			this.pm = pm;
			this.api = api;
			this.ordered = ordered;
		}
		
		public void toSparql(StringBuilder sb) {
			sb.append("  ");
			f.range.op.asSparqlFilter
				( pm
				, f
				, sb
				, "FILTER"
				, api
				, ordered
				, "?" + f.name.asVar()
				, f.range.operands.get(0).asSparqlTerm(pm)
				);
			sb.append("\n");
		}
	}
	
	static class PreSPARQL_Search extends PreSPARQL {
		
		final SearchSpec s;
		final Map<Shortname, Aspect> namesToAspects;
		final PrefixMapping pm;
		
		public PreSPARQL_Search(SearchSpec s, Map<Shortname, Aspect> namesToAspects, PrefixMapping pm) {
			this.pm = pm;
			this.namesToAspects = namesToAspects;
			this.s = s;
		}

		@Override public void toSparql(StringBuilder sb) {
			// sb.append("  { ");
			sb.append("  ");
			sb.append(s.toSearchTriple(namesToAspects, pm));
			sb.append(" .\n");
			// sb.append(" }");
		}
	}
	
	static class PreSPARQL_Union extends PreSPARQL {
		
		final String content;
		
		public PreSPARQL_Union(String content) {
			this.content = content;
		}

		@Override public void toSparql(StringBuilder sb) {
			sb.append(content);
		}
	}
	
	static class PreSPARQL_Comment extends PreSPARQL {
		
		final String message;
		final Object value;
		
		public PreSPARQL_Comment(String message, Object value) {
			this.message = message;
			this.value = value;
		}

		@Override public void toSparql(StringBuilder sb) {	
			sb.append( " # ")
			.append(message)
			.append(" ")
			.append(value)
			.append('\n');			
		}
	}
	
	static class PreSPARQL_Block {

		boolean needsDistinct; 
		List<Aspect> ordered;
		String baseQuery;
		List<String> guards = new ArrayList<String>();
		
		Composition pureFilter;
		final ContextImpl cx;
		
		public void setSelectParameters(boolean needsDistinct, List<Aspect> ordered) {
			this.needsDistinct = needsDistinct;
			this.ordered = ordered;
		}
		
		public void setPureFilter(Composition c) {
			this.pureFilter = c;
		}

		public void setBaseQuery(String baseQuery) {
			this.baseQuery = baseQuery;
		}
		
		public void addGuard(String guardFragment) {
			guards.add(guardFragment);
		}
		
		public void addComment(String message, Object value) {
			append_to_1(new PreSPARQL_Comment(message + " (A)", value));
			append_to_2(new PreSPARQL_Comment(message + " (B)", value));
		}

		public void addSearch(SearchSpec s, Map<Shortname, Aspect> namesToAspects, PrefixMapping pm) {
			append_to_1(new PreSPARQL_Search(s, namesToAspects, pm));
		}
		
		public void addFilter(Filter f, boolean notNested, PrefixMapping pm, API_Dataset api, List<Aspect> ordered) {
			if (cx.impure) append_to_1(new PreSPARQL_Filter(f, notNested, pm, api, ordered));
		}
		
		final List<PreSPARQL> section_1 = new ArrayList<PreSPARQL>();
		final List<PreSPARQL> section_2 = new ArrayList<PreSPARQL>();

		protected void append_to_1(PreSPARQL b) {
			section_1.add(b);
		}
		
		protected void append_to_2(PreSPARQL b) {
			section_2.add(b);
		}
		
		private void declareAspectVars(StringBuilder sb) {
			for (Aspect x: ordered) {
				String fVar = "?" + x.asVar();
			//
				String eqValue = dq.findEqualityValue(pm, x.getName(), c);			
				boolean isEquality = eqValue != null;				
			//		
				sb.append("  ");
			//
				if (x.getIsOptional()) sb.append( " OPTIONAL {" );
				sb
					.append("?item")
					.append(" ").append(x.asProperty())
					.append(" ").append(isEquality ? eqValue : fVar)
					.append(" .")
					;
				if (x.getIsOptional()) sb.append( " }" );
				if (isEquality) {
					sb.append(" BIND(").append(eqValue).append(" AS ").append(fVar).append(")");
				}
				sb.append( "\n" );
			}
		}
		
		public void addUnion(String content) {
			append_to_2(new PreSPARQL_Union(content));
		}

		DataQuery dq;
		PrefixMapping pm;
		Composition c;
		
		public void setCoreParameters(DataQuery dq, PrefixMapping pm, Composition c) {
			this.pm = pm;
			this.c = c;
			this.dq = dq;
		}
		
		public void toSparql(StringBuilder sb) {
			sb.append( "\nSELECT " + (needsDistinct ? "DISTINCT " : "") + "?item");
			for (Aspect x: ordered) sb.append("\n     ?").append( x.asVar() );
		//
			sb.append( "\n {\n" );
		//
			if (baseQuery != null) {
				sb.append( " # BASE QUERY (NEW STYLE)\n");
				sb.append("  { " ).append(baseQuery).append( " }\n" );
			}
		//
			for (String guard: guards) sb.append( " " ).append(guard); 
		//
			declareAspectVars(sb);
		//
			for (PreSPARQL b: section_1) {
				b.toSparql(sb);
			}
		//
			for (PreSPARQL b: section_2) {
				b.toSparql(sb);
			}
		//
			if (pureFilter != null) {
				sb.append( " FILTER" );
				pureFilter.translatePureFilter(sb, cx);
			}
		//
			sb.append( " }\n");
		}
		
		public PreSPARQL_Block(ContextImpl cx) {
			this.cx = cx;
		}
	}
	
	static class ContextImpl implements Context {
		
		final DataQuery dq;
		final Problems p;
		final Set<Aspect> aspects;
		final String baseQuery;
		final PrefixMapping pm;
		final API_Dataset api;
		final boolean impure;
		
		final List<Aspect> ordered = new ArrayList<Aspect>();
		final Map<Shortname, Aspect> namesToAspects = new HashMap<Shortname, Aspect>();
		
		protected PreSPARQL_Block block;
		
		public ContextImpl
			( DataQuery dq
			, Problems p
			, Set<Aspect> aspects
			, String baseQuery
			, PrefixMapping pm
			, API_Dataset api
			, boolean impure
		) {
			this.dq = dq;
			this.p = p;
			this.aspects = aspects;
			this.baseQuery = baseQuery;
			this.pm = pm;
			this.api = api;
			this.impure = impure;
			this.block = new PreSPARQL_Block(this);
		//
			this.ordered.addAll(aspects);
			Collections.sort(this.ordered, Aspect.compareAspects);
		//
			for (Aspect x: aspects) namesToAspects.put(x.getName(), x);
		}
		
		protected ContextImpl fork() {
			return new ContextImpl(dq, /* sb, */ p, aspects, baseQuery, pm, api, impure);
		}

		static final boolean keepComments = true;
		
		@Override public void comment(String message, Object value) {
			if (keepComments) block.addComment(message, value);
		}

		@Override public void addQueryHead() {
			dq.queryHead(this);
		}

		@Override public void addQueryCore() {
			dq.generateQueryCore(this);
		}

		@Override public void addFilter(Filter f, boolean notNested) {
			block.addFilter(f, notNested, pm, api, ordered);
		}

		@Override public void addSearch(SearchSpec s) {
			block.addSearch(s, namesToAspects, pm);
		}

		@Override public void buildPureFilter(StringBuilder sb, Filter f) {
			f.range.op.asSparqlFilter
				( pm
				, f
				, sb
				, ""
				, api
				, ordered
				, "?" + f.name.asVar()
				, f.range.operands.get(0).asSparqlTerm(pm)
				);
		}

		@Override public void topLevelUnion(List<Composition> operands) {			
			ContextImpl cx = this.fork();
			StringBuilder sb2 = new StringBuilder();
			String union = "";
			for (Composition x: operands) {
				sb2.append(union); union = " UNION ";
				sb2.append(" {{\n");
				x.topLevel(cx);
				cx.block.toSparql(sb2);
				sb2.append( " }}\n" );
			}
			block.addUnion(sb2.toString());
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

	private void queryHead(ContextImpl cx) {
		List<Aspect> ordered = cx.ordered;
		PreSPARQL_Block block = cx.block;
	//	
		boolean needsDistinct = false;
        for (Guard guard : guards) if (guard.needsDistinct()) needsDistinct = true;
    //
		block.setSelectParameters(needsDistinct, ordered);
	}

	private void generateQueryCore(ContextImpl cx) {
		String baseQuery = cx.baseQuery;
		PrefixMapping pm = cx.pm;
		API_Dataset api = cx.api;
		PreSPARQL_Block block = cx.block;
	//
        boolean baseQueryNeeded = true;  
        for (Guard guard : guards) {
            if (guard.supplantsBaseQuery()) {
                baseQueryNeeded = false;
            }
        }
    //
		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded) {
		    block.setBaseQuery( baseQuery );
		}
	//
	    for (Guard guard : guards) {
	        block.addGuard(guard.queryFragment(api));
	    }
	//
	    block.setCoreParameters(this, pm, c);
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