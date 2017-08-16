/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.List;
import java.util.Set;

public class SQ_Triple implements SQ_WhereElement {

	final SQ_Node S, P, O;
	
	public SQ_Triple(SQ_Node S, SQ_Node P, SQ_Node O) { 
		this.S = S; this.P = P; this.O = O; 
	}

	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent);
		renderRawCoreTriple(sb);
		sb.append(" .");
		sb.append(SQ.nl);
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof SQ_Triple && same( (SQ_Triple) other );
	}
	
	static int count = 0;
	
	private boolean same(SQ_Triple other) {
		boolean result = S.equals(other.S) && P.equals(other.P) && O.equals(other.O);
		return result;
	}

	@Override public int hashCode() {
		// a better hashcode probably isn't required for this context
		return S.hashCode() + P.hashCode() + O.hashCode();
	}
	
	@Override public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("(");
		renderRawCoreTriple(result);
		result.append(")");
		return result.toString();
	}

	void renderRawCoreTriple(StringBuilder sb) {
		S.toSparqlExpr(sb);
		sb.append(" ");
		P.toSparqlExpr(sb);
		sb.append(" ");
		O.toSparqlExpr(sb);
	}

	public SQ_WhereElement optional() {
		return new OptionalTriple(this);
	}

	public static SQ_WhereElement optionals(final List<SQ_Triple> ts) {
		return new OptionalTriples(ts);
	}

	@Override public void updateVars(Set<String> varNames) {
		updateVars(varNames, S);
		updateVars(varNames, P);
		updateVars(varNames, O);
	}
	
	private void updateVars(Set<String> varNames, SQ_Node x) {
		if (x instanceof SQ_Variable) varNames.add(((SQ_Variable) x).asVar());
	}

	private static final class OptionalTriples implements SQ_WhereElement {
		private final List<SQ_Triple> ts;

		private OptionalTriples(List<SQ_Triple> ts) {
			this.ts = ts;
		}
		
		@Override public int hashCode() {
			return ts.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof OptionalTriples && same((OptionalTriples) other);
		}

		private boolean same(OptionalTriples other) {
			return ts.equals(other.ts);
		}

		@Override public void toSparqlStatement(StringBuilder sb, String indent) {
			sb.append(indent);
			sb.append("OPTIONAL { ");
			for (SQ_Triple t: ts) { 
				t.renderRawCoreTriple(sb); sb.append( " . "); 
			}
			sb.append("}").append(SQ.nl);
		}

		@Override public void updateVars(Set<String> varNames) {
			// optionals don't bind all names
		}
	}

	private final class OptionalTriple implements SQ_WhereElement {
		
		private final SQ_Triple t;

		private OptionalTriple(SQ_Triple t) {
			this.t = t;
		}
		
		@Override public int hashCode() {
			return t.hashCode();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof OptionalTriple && same((OptionalTriple) other);
		}

		private boolean same(OptionalTriple other) {
			return t.equals(other.t);
		}

		@Override public void toSparqlStatement(StringBuilder sb, String indent) {
			sb.append(indent);
			sb.append("OPTIONAL { ");
			t.renderRawCoreTriple(sb);
			sb.append("}").append(SQ.nl);
		}

		@Override public void updateVars(Set<String> varNames) {
			// optional triples don't always bind names
		}
	}
}