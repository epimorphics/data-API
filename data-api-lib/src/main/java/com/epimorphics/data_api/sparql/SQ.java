/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.epimorphics.data_api.data_queries.Sort;
import com.hp.hpl.jena.vocabulary.XSD;

public class SQ {
	
	public static final String nl = "\n";
	
	final List<SQ_SelectedVar> selected = new ArrayList<SQ_SelectedVar>();
	
	final SQ_Where whereClause = new SQ_Where();
	
	Integer limit;
	
	Integer offset;
	
	String baseQuery;
	
	final List<Sort> sorts = new ArrayList<Sort>();
	
	public SQ() {
		
	}

	@Override public String toString() {
		return toString("");
	}
	
	public String toString(String indent) {
		StringBuilder sb = new StringBuilder();
		toString(sb, indent);
		return sb.toString();
	}
	
	public void toString(StringBuilder sb, String indent) {
		sb.append(indent).append("SELECT").append(nl);
		
		for (SQ_SelectedVar v: selected) {
			sb.append(indent).append("  ");
			if (v.needsDistinct) sb.append("DISTINCT ");
			sb.append(v.var.asVar()).append(nl);
		}
		
		sb.append(indent).append("WHERE").append(nl);
		sb.append(indent).append("{").append(nl);
		if (baseQuery != null) sb.append(indent).append(baseQuery).append(nl);
		whereClause.toString(sb, indent + "  ");
		sb.append(indent).append("}").append(nl);
		querySort(sb, indent, sorts);
		if (limit != null) sb.append(indent).append("LIMIT").append(" ").append(limit).append(nl);
		if (offset != null) sb.append(indent).append("OFFSET").append(" ").append(offset).append(nl);
	}
	
	protected void querySort(StringBuilder sb, String indent, List<Sort> sortby) {
		if (sortby.size() > 0) {
			sb.append(indent).append("ORDER BY");
			for (Sort s: sortby) {
				sb.append(" ");
				s.toString(sb);
			}
		}
	}
	
	public void addSelectedVar(SQ_Variable v) {
		addSelectedVar(v, false);
	}
	
	public void addSelectedVar(SQ_Variable v, boolean needsDistinct) {
		selected.add(new SQ_SelectedVar(v, needsDistinct));
	}
	
	public void addBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}

	public void addNotExists(SQ_Triple t, SQ_Filter f) {
		whereClause.add(new SQ_NotExists(t, f));
	}

	public void addNotExists(SQ_Triple t) {
		whereClause.add(new SQ_NotExists(t));
	}

	public void addQueryFragment(String queryFragment) {
		whereClause.add(new SQ_Fragment(queryFragment));
	}
	
	public void addOptionalTriple(SQ_Triple t) {
		whereClause.addOptionalTriple(t);
	}
	
	public void addOptionalTriples(List<SQ_Triple> ts) {
		whereClause.addOptionalTriples(ts);
	}
	
	public void addTriple(SQ_Triple t) {
		// System.err.println(">> adding triple: " + t);
		whereClause.addTriple(t);
	}
	
	public void addTriples(List<SQ_Triple> ts) {
		// System.err.println(">> adding triples: " + ts);
		for (SQ_Triple t: ts) whereClause.addTriple(t);
	}

	public void addWhereElement(SQ_WhereElement e) {
		whereClause.add(e);
	}
	
	public void addFilter(SQ_Filter f) {
		whereClause.addFilter(f);
	}
	
	public void addOptionalFilter(SQ_WhereElement f) {
		whereClause.addOptionalFilter(f);
	}

	public void addFalse() {
		whereClause.add(SQ_FalseFilter.value);
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void addBind(SQ_Node value, SQ_Variable var) {
		whereClause.addBind(value, var);
	}
	
	public void comment(String message, Object... values) {
		whereClause.addComment
			( values.length == 0 
			? message 
			: message + " " + Arrays.asList(values).toString()
			);
	}
	
	public void addSorts(List<Sort> sorts) {
		this.sorts.addAll(sorts);
	}

	public void addSubquery(SQ nested) {
		whereClause.addSubquery(nested);
	}

	static final String XSD_integer = XSD.getURI() + "integer";
	
	public static SQ_Literal integer(int n) {
		return new SQ_Literal("" + n, XSD_integer); // TODO
	}

	public static SQ_NodeList list(SQ_Node ...elements) {
		return new SQ_NodeList(elements);
	}

	public void addSqFilter(SQ_Expr toAdd) {
		whereClause.addSqFilter(toAdd);
	}
	
}