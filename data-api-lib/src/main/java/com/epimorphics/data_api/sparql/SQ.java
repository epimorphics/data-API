/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.sparql;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Operator.EqOperator;
import com.epimorphics.data_api.data_queries.Sort;
import com.epimorphics.data_api.data_queries.Operator.FunctionOperator;
import com.epimorphics.data_api.sparql.SQ.Expr;
import com.epimorphics.data_api.sparql.SQ.Node;
import com.hp.hpl.jena.sparql.util.FmtUtils;

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
	
	public void addOptionalTriple(SQ.Triple t) {
		whereClause.addOptionalTriple(t);
	}

	public void addWhereElement(WhereElement e) {
		whereClause.add(e);
	}
	
	public void addFilter(SQ.FilterSQ f) {
		whereClause.addFilter(f);
	}
	
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void addBind(Expr value, Variable var) {
		whereClause.addBind(value, var);
	}
	
	public void addSorts(List<Sort> sorts) {
		this.sorts.addAll(sorts);
	}
	
/////////////////////////////////////////////////////////////////

	static final String nl = "\n";
				
	public interface Node extends Expr {
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
			sb.append("").append(uri()).append("").append(" ");
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
		
		protected String safeSpelling() {
			return FmtUtils.stringEsc(spelling, true);
		}
		
		@Override public void toString(StringBuilder sb) {
			sb.append("\"").append(safeSpelling()).append("\"");
			if (!type.isEmpty()) sb.append("^^").append(type);
			sb.append(" ");
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

		public WhereElement optional() {
			final Triple it = this;
			return new WhereElement() {

				@Override public void toString(StringBuilder sb, String indent) {
					sb.append(indent);
					sb.append("OPTIONAL {");
					it.toString(sb, "");
					sb.append("}").append(nl);
				}};
		}
	}
	
	public interface Expr {
		public void toString(StringBuilder sb);
	}
	
	public abstract static class FilterSQ implements WhereElement {

		@Override public abstract void toString(StringBuilder sb, String indent);
		
		public abstract void toStringNoFILTER(StringBuilder sb);
		
	}	
	
	public static class OpFilter extends FilterSQ {
		
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
			toStringNoFILTER(sb);
			sb.append(")");
			sb.append(nl);
		}

		/* @Override */ public void toStringNoFILTER(StringBuilder sb) {
			
			L.toString(sb);
			sb.append(" ").append(op).append(" ");
			R.toString(sb);
		}
	}
	
	public static class FunFilter extends FilterSQ {
		
		final Expr L;
		final Operator op;
		final Expr R;
		
		public FunFilter(Expr L, Operator op, Expr R) {
			this.L = L;
			this.op = op;
			this.R = R;
		}

		@Override public void toString(StringBuilder sb, String indent) {
			sb.append(indent);
			sb.append("FILTER(");
			toStringNoFILTER(sb);
			sb.append(")");
			sb.append(nl);
		}

		/* @Override */ public void toStringNoFILTER(StringBuilder sb) {
			
//			L.toString(sb);
//			sb.append(" ").append(op).append(" ");
//			R.toString(sb);
			
			if (op instanceof FunctionOperator) {
				String name = ((FunctionOperator) op).functionName;
				sb.append(name);
				sb.append("(");
				L.toString(sb);
				sb.append(", ");
				R.toString(sb);
				sb.append(")");	
			} else if (op instanceof EqOperator) {
				String name = ((EqOperator) op).asInfix();
				L.toString(sb);
				sb.append(" = ");
				R.toString(sb);				
			} else {
				throw new RuntimeException("" + op.getClass().getSimpleName());
			}
		}
	}
	
	public interface WhereElement {

		public void toString(StringBuilder sb, String indent);
		
	}
	
	public static class Bind implements WhereElement {

		final Expr value;
		final Variable var;
		
		public Bind(Expr value, Variable var) {
			this.value = value;
			this.var = var;
		}
		
		@Override public void toString(StringBuilder sb, String indent) {
			sb.append(indent).append("BIND(");
			value.toString(sb);		
			sb.append(" AS  ");
			var.toString(sb);
			sb.append(")").append(nl);
		}
		
	}
	
	public static class Where {

		final List<WhereElement> elements = new ArrayList<WhereElement>();
		
		public void toString(StringBuilder sb, String indent) {
			for (WhereElement e: elements)
				e.toString(sb, indent);
		}

		public void add(WhereElement e) {
			elements.add(e);
		}

		public void addBind(Expr value, Variable var) {
			elements.add(new Bind(value, var));
		}

		public void addTriple(SQ.Triple t) {
			elements.add(t);
		}

		public void addOptionalTriple(SQ.Triple t) {
			elements.add(t.optional());
		}

		public void addFilter(SQ.FilterSQ f) {
			elements.add(f);
		}
		
	}
	
}