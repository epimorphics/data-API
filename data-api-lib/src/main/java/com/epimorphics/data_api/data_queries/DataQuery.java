/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.Composition.And;
import com.epimorphics.data_api.data_queries.Composition.Filters;
import com.epimorphics.data_api.data_queries.Composition.Or;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.util.FmtUtils;

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
	final String globalSearchPattern;
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
    	this(c, sortby, guards, slice, null);
    }

    public DataQuery(Composition c, List<Sort> sortby, List<Guard> guards, Slice slice, String globalSearchPattern) {
		this.c = c;
		this.sortby = sortby;
		this.slice = slice;
		this.guards = guards == null ? new ArrayList<Guard>(0) : guards;
		this.globalSearchPattern = globalSearchPattern;
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

	public String getGlobalSearchPattern() {
		return globalSearchPattern;
	}
	
	public List<Filter> filters() {
		if (c instanceof Filters) return ((Filters) c).filters;
		return new ArrayList<Filter>();
	}
    
    public String toSparql(Problems p, API_Dataset api) {
        try { return toSparqlString(p, api.getAspects(), api.getBaseQuery(), api.getPrefixes(), api); }
        catch (Exception e) { p.add("exception generating SPARQL query: " + e.getMessage()); e.printStackTrace(System.err); return null; }
    }
    
    public String toSparql(Problems p, Aspects a, String baseQuery, PrefixMapping pm) {
        try { return toSparqlString(p, a.getAspects(), baseQuery, pm, null); }
        catch (Exception e) { p.add("exception generating SPARQL query: " + e.getMessage()); e.printStackTrace(System.err); return null; }
    }

	static final Term item = Term.var("item");
	
	static final boolean properly = true;

	private String toSparqlString(Problems p, Set<Aspect> a, String baseQuery, PrefixMapping pm, API_Dataset api) {
		List<Aspect> ordered = new ArrayList<Aspect>(a);
		Collections.sort(ordered, compareAspects);
		return newToSparqlString(ordered, p, a, baseQuery, pm, api);
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

	private String newToSparqlString(List<Aspect> ordered, Problems p, Set<Aspect> a,	String baseQuery, PrefixMapping pm, API_Dataset api) {
		StringBuilder sb = new StringBuilder();
		String core = queryCore(p, a, baseQuery, pm, api);
		String head = queryHead(p, a, baseQuery, pm, api);
				
		sb.append(head).append("\n");
		recursiveTranslate(true, ordered, c, head, core, sb, baseQuery, pm, api);
		querySort(sb);
		
		if (slice.length != null) sb.append( " LIMIT " ).append(slice.length);
		if (slice.offset != null) sb.append( " OFFSET " ).append(slice.offset);
		
		return PrefixUtils.expandQuery(sb.toString(), pm);
	}

	private void recursiveTranslate
		( boolean needsHead, List<Aspect> ordered, Composition c, String head, String core, StringBuilder sb, String baseQuery, PrefixMapping pm, API_Dataset api) {
		
		if (c instanceof Or) {
			// sb.append(head);
			String union = "";
			for (Composition o: c.operands) {
				sb.append(union); union = " UNION ";
				
				sb.append( " {\n" );
				sb.append( "{" ); sb.append(head);  
				sb.append( " WHERE \n{ "); 
				sb.append(core);
				recursiveTranslate(false, ordered, o, head, core, sb, baseQuery, pm, api);
				sb.append( " \n}}" );
				
			}
			sb.append( "\n}}");
		} else  {
			if (needsHead) sb.append( " WHERE \n{ ").append(core);
			if (c instanceof And) {
				for (Composition o: c.operands) {
					recursiveTranslate(false, ordered, o, head, core, sb, baseQuery, pm, api);
				}
			} else if (c instanceof Filters) {	
			//
				String dot = "";
				for (Filter f: ((Filters) c).filters) {
					sb.append(dot);
					doFilter(dot, sb, f, api, ordered, pm);
					dot = " . ";
				}
			} else if (c.op.equals("none")) {
				//
			} else {
				throw new UnsupportedOperationException("Cannot recursively translate: " + c);
			}
			if (needsHead) sb.append(" } ");
		}
	}

//	private void headAndCore(String head, String core, StringBuilder sb) {
//		sb.append(head);  
//		sb.append( " WHERE \n{ "); 
//		sb.append(core);
//	}

	private void doFilter(String dot, StringBuilder sb, Filter f, API_Dataset api, List<Aspect> ordered, PrefixMapping pm) {
		String key = "?" + f.name.asVar();
		String fVar = key;
		String value = f.range.operands.get(0).asSparqlTerm(pm);
		String rangeOp = f.getRangeOp();
		
		if (rangeOp.equals("oneof")) {
			String orOp = "";
			List<Term> operands = f.range.operands;
			sb.append(" FILTER(" );
			for (Term v: operands) {
				sb.append(orOp).append(fVar).append( " = ").append(v.asSparqlTerm(pm));
				orOp = " || ";
			}
			sb.append(")");
		
		} else if (rangeOp.equals("below")) {		
			Aspect x = aspectFor(ordered, f.name);
			String below = x.getBelowPredicate(api);
			sb.append(". ").append(value).append(" ").append(below).append("* ").append(fVar);
		
		} else if (rangeOp.equals("contains")) {
			sb.append(" FILTER(").append("CONTAINS(").append(fVar).append(", ").append(value).append(")").append(")");
		
		} else if (rangeOp.equals("matches")) {
			sb.append(" FILTER(").append("REGEX(").append(fVar).append(", ").append(value).append(")").append(")");
		
		} else if (rangeOp.equals("search")) {
			// System.err.println( ">> 'search' suppressed" );
			 // sb.append(" TRUE ");
			 sb.append(". ").append(fVar).append(" <http://jena.apache.org/text#query> ").append(value);
		
		} else if (rangeOp.equals("eq")) {
//			sb.append("?item ").append(f.name.prefixed).append( " ").append(value);
//			sb.append(" BIND(").append(value).append(" AS ").append(fVar).append(")");
		
		} else {
			String op = opForFilter(f);
			sb.append(" FILTER(" ).append(fVar).append(" ").append(op).append(" ").append(value).append(")");
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
		
		StringBuilder sb = new StringBuilder();
        boolean baseQueryNeeded = true;
        
        for (Guard guard : guards) {
            if (guard.supplantsBaseQuery()) {
                baseQueryNeeded = false;
            }
        }
		String dot = "";
	//
	    if (globalSearchPattern != null) {
	        sb.append(dot).append("?item").append(" <http://jena.apache.org/text#query> ").append(quote(globalSearchPattern));
	        dot = " . ";
	    }
	//
		if (baseQuery != null && !baseQuery.isEmpty() && baseQueryNeeded) {
		    sb.append(dot).append( baseQuery );
		    if (!baseQuery.trim().endsWith(".")) {
		        dot = ".\n";
		    }
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
			boolean isEquality = eqValue != null; // theseFilters != null && theseFilters.get(0).range.op.equals("eq");						
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
		if (c instanceof Filters) {
			for (Filter f: ((Filters) c).filters) {
				if (f.name.prefixed.equals(name.prefixed))
					if (f.range.op.equals("eq")) 
						return f.range.operands.get(0).asSparqlTerm(pm);
			}
		}
		for (Composition x: c.operands) {
			String eq = findEqualityValue(pm, name, x);
			if (eq != null) return eq;
		}
		return null;
	}

	private Aspect aspectFor(List<Aspect> ordered, Shortname name) {
		for (Aspect a: ordered) if (a.getName().equals(name)) return a;
		throw new BrokenException("Could not find aspect " + name + " in " + ordered);
	}

	/**
	    Quote a string to turn in into a SPARQL term.
	*/
	private String quote(String s) {
		return "\"" + FmtUtils.stringEsc(s, true) + "\"";
	}

	private String opForFilter(Filter f) {
		String rangeOp = f.getRangeOp();
		return opFor(rangeOp);
	}
	
	private String opFor(String rangeOp) {
		if (rangeOp.equals("eq")) return "=";
		if (rangeOp.equals("ne")) return "!=";
		if (rangeOp.equals("le")) return "<=";
		if (rangeOp.equals("lt")) return "<";
		if (rangeOp.equals("ge")) return ">=";
		if (rangeOp.equals("gt")) return ">";
		throw new RuntimeException("should never happen: unexpected range op '" + rangeOp + "'");
	}
}