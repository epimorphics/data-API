/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.sparql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Sort;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.vocabulary.XSD;

public class SQ {
	
	public static class Const {
		public static final Variable item = new Variable("item");
		public static final Node textQuery = new Resource("http://jena.apache.org/text#query");
		public static final List<Expr> NONE = Collections.<Expr>emptyList();
		
	}

	public static class Output {
	
		public final SQ.Variable var;
		public final boolean needsDistinct;
		
		public Output(SQ.Variable var, boolean needsDistinct) {
			this.var = var;
			this.needsDistinct = needsDistinct;
		}
	}
	
	final List<Output> variables = new ArrayList<Output>();
	
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
		
		for (Output v: variables) {
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
	
	public void addOutput(SQ.Variable v) {
		addOutput(v, false);
	}
	
	public void addOutput(SQ.Variable v, boolean needsDistinct) {
		variables.add(new Output(v, needsDistinct));
	}
	
	public void addBaseQuery(String baseQuery) {
		this.baseQuery = baseQuery;
	}

	public void addQueryFragment(String queryFragment) {
		whereClause.add(new Fragment(queryFragment));
	}
	
	public void addTriple(SQ.Triple t) {
		whereClause.addTriple(t);
	}


	public void addNotExists(Triple t) {
		whereClause.add(new NotExists(t));
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

	static final String XSD_integer = XSD.getURI() + "integer";
	
	public static Literal integer(int n) {
		return new Literal("" + n, XSD_integer); // TODO
	}

	public static TermList list(Node ...elements) {
		return new TermList(elements);
	}
	
/////////////////////////////////////////////////////////////////

	static final String nl = "\n";
				
	public static abstract class Node implements Expr {

		@Override public List<Expr> operands() {
			return Const.NONE;
		}
		
	}
	
	// TODO add operator slot of some kind (NOT a String, 
	// maybe the existing Operator
	public interface Expr {
		public void toSparqlExpr(StringBuilder sb);
		
		public List<Expr> operands();
	}
	
	public static class TermList extends Node {

		final Node[] elements;
		
		public TermList(Node ...elements) {
			this.elements = elements;
		}
		
		@Override public void toSparqlExpr(StringBuilder sb) {
			String gap = "";
			sb.append("(");
			for (Node e: elements) {
				sb.append(gap);	gap = " ";
				e.toSparqlExpr(sb);
			}
			sb.append(")");
		}
	}
	
	public static class Fragment implements WhereElement {

		final String content;
		
		public Fragment(String content) {
			this.content = content;
		}
		
		@Override public void toString(StringBuilder sb, String indent) {
			sb.append(indent).append(content).append(nl);
		}
		
	}
	
	public static class NotExists implements WhereElement {
		
		final Triple t;
		
		public NotExists(Triple t) {
			this.t = t;
		}

		@Override public void toString(StringBuilder sb, String indent) {
			sb.append(indent).append("FILTER(NOT EXISTS {");
			t.toString(sb, "");
			sb.append("})").append(nl);
		}
		
	}
	
	public static class Resource extends Node {

		final String uri;
		
		public Resource(String uri) {
			this.uri = uri;
		}
		
		public String uri() {
			return uri;
		}
		
		@Override public void toSparqlExpr(StringBuilder sb) {
			if (uri.startsWith("http:") || uri.startsWith("eh:")) {
				System.err.println(">> TODO: fix this fragile absolute-uri test.");
				sb.append("<").append(uri()).append(">").append(" ");
			} else {
				sb.append("").append(uri()).append("").append(" ");
			}
		}
		
	} 
	
	public static class Literal extends Node {

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
		
		@Override public void toSparqlExpr(StringBuilder sb) {
			sb.append("\"").append(safeSpelling()).append("\"");
			if (!type.isEmpty()) sb.append("^^<").append(type).append(">");
			sb.append(" ");
		}
	}
	
	public static class Variable extends Node {
		
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

		@Override public void toSparqlExpr(StringBuilder sb) {
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
			S.toSparqlExpr(sb);
			P.toSparqlExpr(sb);
			O.toSparqlExpr(sb);
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
	
	public static class FilterSQ implements WhereElement {
		
		final List<Expr> operands;
		final Variable x;
		final Operator op;
		
		public FilterSQ(Operator op, Variable x, List<Expr> operands) {
			this.op = op;
			this.x = x;
			this.operands = operands;
		}
				
		@Override public void toString(StringBuilder sb, String indent) {
			sb.append(indent);
			sb.append("FILTER(");
			toStringNoFILTER(sb);
			sb.append(")");
			sb.append(nl);
		}		
		
		public void toStringNoFILTER(StringBuilder sb) {
			op.asExpression(sb, x, operands);
		}

	}	
	
//	public static class OpFilter extends FilterSQ {
//		
//		final Expr L;
//		final String op;
//		final Expr R;
//		
//		public OpFilter(Expr L, String op, Expr R) {
//			this.L = L;
//			this.op = op;
//			this.R = R;
//		}
//
//		@Override public void toString(StringBuilder sb, String indent) {
//			sb.append(indent);
//			sb.append("FILTER(");
//			toStringNoFILTER(sb);
//			sb.append(")");
//			sb.append(nl);
//		}
//
//		/* @Override */ public void toStringNoFILTER(StringBuilder sb) {
//			
//			L.toString(sb);
//			sb.append(" ").append(op).append(" ");
//			R.toString(sb);
//		}
//	}
//	
//	public static class FunFilter extends FilterSQ {
//		
//		final Expr L;
//		final Operator op;
//		final Expr R;
//		
//		public FunFilter(Expr L, Operator op, Expr R) {
//			this.L = L;
//			this.op = op;
//			this.R = R;
//		}
//
//		@Override public void toString(StringBuilder sb, String indent) {
//			sb.append(indent);
//			sb.append("FILTER(");
//			toStringNoFILTER(sb);
//			sb.append(")");
//			sb.append(nl);
//		}
//
//		/* @Override */ public void toStringNoFILTER(StringBuilder sb) {
//			
////			L.toString(sb);
////			sb.append(" ").append(op).append(" ");
////			R.toString(sb);
//			
//			if (op instanceof FunctionOperator) {
//				String name = ((FunctionOperator) op).functionName;
//				sb.append(name);
//				sb.append("(");
//				L.toString(sb);
//				sb.append(", ");
//				R.toString(sb);
//				sb.append(")");	
//			} else if (op instanceof EqOperator) {
//				String name = ((EqOperator) op).asInfix();
//				L.toString(sb);
//				sb.append(" = ");
//				R.toString(sb);				
//			} else {
//				throw new RuntimeException("" + op.getClass().getSimpleName());
//			}
//		}
//	}
	
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
			value.toSparqlExpr(sb);		
			sb.append(" AS ");
			var.toSparqlExpr(sb);
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