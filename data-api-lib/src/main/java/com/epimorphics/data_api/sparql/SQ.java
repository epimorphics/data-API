/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.epimorphics.data_api.data_queries.Modifiers;
import com.epimorphics.data_api.data_queries.Sort;
import com.hp.hpl.jena.vocabulary.XSD;

public class SQ {
	
	public static final String nl = "\n";
	
	final List<SQ_SelectedVar> selected = new ArrayList<SQ_SelectedVar>();
	
	final SQ_Where whereClause = new SQ_Where();
	
	Modifiers queryModifiers;
	
	Modifiers itemModifiers;
	
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
		appendSelection(sb, indent);
		
		sb.append(indent).append("WHERE").append(nl);
		sb.append(indent).append("{").append(nl);
		if (baseQuery != null) sb.append(indent).append(baseQuery).append(nl);
		whereClause.toString(sb, indent + "  ", this);
		sb.append(indent).append("}").append(nl);
		queryModifiers.render(sb);
	}

	void appendSelection(StringBuilder sb, String indent) 
		{
		for (SQ_SelectedVar v: selected) {
			sb.append(indent).append("  ");
			if (v.needsDistinct) sb.append("DISTINCT ");
			sb.append(v.var.asVar()).append(nl);
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
	
	public void addSearchTriple(SQ_Triple t) {
		whereClause.addSearchTriple(t);
	}
	
	public void addTriple(SQ_Triple t) {
		whereClause.addTriple(t);
	}
	
	public void addTriples(List<SQ_Triple> ts) {
		for (SQ_Triple t: ts) whereClause.addTriple(t);
	}

	public void addWhereElement(SQ_WhereElement e) {
		whereClause.add(e);
	}
	
	public void addFilter(SQ_Filter f) {
		whereClause.addFilter(f);
	}

	public void addFalse() {
		whereClause.add(SQ_FalseFilter.value);
	}
	
	public void setQueryModifiers(Modifiers queryModifiers) {
		this.queryModifiers = queryModifiers;
	}
	
	public void setItemModifiers(Modifiers itemModifiers) {
		this.itemModifiers = itemModifiers;
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