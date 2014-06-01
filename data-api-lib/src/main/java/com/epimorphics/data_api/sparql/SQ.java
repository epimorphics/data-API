/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.sparql;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.data_queries.Sort;

public class SQ {

	final List<SQ.Variable> variables = new ArrayList<SQ.Variable>();
	
	final SQ.Where whereClause = new Where();
	
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
		for (SQ.Variable v: variables) sb.append(indent).append("  ").append(v.asVar()).append(nl);
		sb.append(indent).append("WHERE").append(nl);
		sb.append(indent).append("{").append(nl);
		if (baseQuery != null) sb.append(indent).append(baseQuery);
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
	
	public void addOutput(SQ.Variable v) {
		variables.add(v);
	}
	
	public void addBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}
	
	public void addTriple(SQ.Triple t) {
		whereClause.addTriple(t);
	}
	
	public void addFilter(SQ.OpFilter f) {
		whereClause.addFilter(f);
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public void addSorts(List<Sort> sorts) {
		this.sorts.addAll(sorts);
	}
	
/////////////////////////////////////////////////////////////////

	static final String nl = "\n";
				
	public interface Node {
		public void toString(StringBuilder sb);
	}
	
	public static class Resource implements Node, Expr {

		final String uri;
		
		public Resource(String uri) {
			this.uri = uri;
		}
		
		public String uri() {
			return uri;
		}
		
		@Override public void toString(StringBuilder sb) {
			sb.append("<").append(uri()).append(">").append(" ");
		}
		
	} 
	
	public static class Literal implements Node, Expr {

		final String spelling;
		final String type;
		
		public Literal(String spelling, String type) {
			this.spelling = spelling;
			this.type = type;
		}
		
		public String spelling() {
			return spelling;
		}
		
		public String type() {
			return type;
		}
		
		@Override public void toString(StringBuilder sb) {
			sb
				.append("\"").append(spelling()).append("\"")
				.append("^^").append(type).append(" ")
				;
		}
		
	}
	
	public static class Variable implements Node, Expr {
		
		final String name;
		
		public Variable(String name) {
			this.name = name;
		}
		
		public String name() {
			return name;
		}
		
		public String asVar() {
			return "?" + name;
		}

		@Override public void toString(StringBuilder sb) {
			sb.append("?").append(name()).append(" ");
		}
		
	}
	
	public static class Triple implements WhereElement {
		final Node S, P, O;
		
		public Triple(Node S, Node P, Node O) { 
			this.S = S; this.P = P; this.O = O; 
		}

		@Override public void toString(StringBuilder sb, String indent) {
			sb.append(indent);
			S.toString(sb);
			P.toString(sb);
			O.toString(sb);
			sb.append(" .");
			sb.append(nl);
		}
	}
	
	public interface Expr {
		public void toString(StringBuilder sb);
	}
	
	public static class OpFilter implements WhereElement {
		
		final Expr L;
		final String op;
		final Expr R;
		
		public OpFilter(Expr L, String op, Expr R) {
			this.L = L;
			this.op = op;
			this.R = R;
		}

		@Override public void toString(StringBuilder sb, String indent) {
			sb.append(indent);
			sb.append("FILTER(");
			L.toString(sb);
			sb.append(" ").append(op).append(" ");
			R.toString(sb);
			sb.append(")");
			sb.append(nl);
		}
	}
	
	public interface WhereElement {

		public void toString(StringBuilder sb, String indent);
		
	}
	
	public static class Where {

		final List<WhereElement> elements = new ArrayList<WhereElement>();
		
		public void toString(StringBuilder sb, String indent) {
			for (WhereElement e: elements)
				e.toString(sb, indent);
		}

		public void addTriple(SQ.Triple t) {
			elements.add(t);
		}

		public void addFilter(SQ.OpFilter f) {
			elements.add(f);
		}
		
	}
	
}