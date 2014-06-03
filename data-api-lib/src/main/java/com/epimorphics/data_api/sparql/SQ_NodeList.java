/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

public class SQ_NodeList extends SQ_Node {

	final SQ_Node[] elements;
	
	public SQ_NodeList(SQ_Node ...elements) {
		this.elements = elements;
	}
	
	@Override public void toSparqlExpr(StringBuilder sb) {
		String gap = "";
		sb.append("(");
		for (SQ_Node e: elements) {
			sb.append(gap);	gap = " ";
			e.toSparqlExpr(sb);
		}
		sb.append(")");
	}
}