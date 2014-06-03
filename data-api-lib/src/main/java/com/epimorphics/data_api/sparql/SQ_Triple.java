/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;


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

	void renderRawCoreTriple(StringBuilder sb) {
		S.toSparqlExpr(sb);
		P.toSparqlExpr(sb);
		O.toSparqlExpr(sb);
	}

	public SQ_WhereElement optional() {
		final SQ_Triple it = this;
		return new SQ_WhereElement() {

			@Override public void toSparqlStatement(StringBuilder sb, String indent) {
				sb.append(indent);
				sb.append("OPTIONAL { ");
				it.renderRawCoreTriple(sb);
				sb.append("}").append(SQ.nl);
			}};
	}
}