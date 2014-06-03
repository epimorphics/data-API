/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;


public class SQ_NotExists implements SQ_WhereElement {
	
	final SQ_Triple t;
	final SQ_Filter f;
	
	public SQ_NotExists(SQ_Triple t) {
		this(t, null);
	}
	
	public SQ_NotExists(SQ_Triple t, SQ_Filter f) {
		this.t = t;
		this.f = f;
	}

	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent).append("FILTER(NOT EXISTS {");
		t.renderRawCoreTriple(sb);
		if (f != null) f.toSparqlStatement(sb, "");
		sb.append("})").append(SQ.nl);
	}
	
}