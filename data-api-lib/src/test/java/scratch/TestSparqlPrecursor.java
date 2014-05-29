/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package scratch;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import scratch.TestSparqlPrecursor.SQ.OpFilter;

public class TestSparqlPrecursor {
	
	@Test public void testingPrecursor() {
		
		SQ q = new SQ();
		System.err.println(">>\n" + q.toString());
		
		SQ q2 = new SQ();
		q2.addOutput(new SQ.Variable("?alpha"));
		System.err.println(">>\n" + q2.toString());
		
		SQ q3 = new SQ();
		q3.addOutput(new SQ.Variable("?alpha"));
		q3.addOutput(new SQ.Variable("?beta"));
		System.err.println(">>\n" + q3.toString());
		
	}
	
	@Test public void testing2() {
		SQ q = new SQ();
		q.addOutput(new SQ.Variable("?alpha"));
		SQ.Node S1 = new SQ.Resource("eh:/alpha");
		SQ.Node S2 = new SQ.Resource("eh:/beta");
		SQ.Node P = new SQ.Variable("?item");
		SQ.Node O = new SQ.Literal("eh:/alpha", "xsd:string");
		
		q.addTriple(new SQ.Triple(S1, P, O));
		q.addTriple(new SQ.Triple(S2, P, O));
		
		System.err.println(">>\n" + q.toString());
	}
	
	@Test public void testing3() {
		SQ q = new SQ();
		SQ.Variable A = new SQ.Variable("?A");
		SQ.Node P = new SQ.Resource("eh:/P");
		SQ.Literal V = new SQ.Literal("17", "xsd:integer");
		q.addFilter(new SQ.OpFilter(A, "==", V));		
		q.addTriple(new SQ.Triple(A, P, V));
		System.err.println(">>\n" + q.toString());

	}
	
	public static class SQ {
	
		final List<Variable> variables = new ArrayList<Variable>();
		
		final Where whereClause = new Where();
		
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
			for (Variable v: variables) sb.append(indent).append("  ").append(v.name()).append(nl);
			sb.append(indent).append("WHERE").append(nl);
			sb.append(indent).append("{").append(nl);
			whereClause.toString(sb, indent + "  ");
			sb.append(indent).append("}").append(nl);
		}
		
		public void addOutput(Variable v) {
			variables.add(v);
		}
		
		public void addTriple(Triple t) {
			whereClause.addTriple(t);
		}
		
		public void addFilter(OpFilter f) {
			whereClause.addFilter(f);
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

			public void addTriple(Triple t) {
				elements.add(t);
			}

			public void addFilter(OpFilter f) {
				elements.add(f);
			}
			
		}
		
	}

}
