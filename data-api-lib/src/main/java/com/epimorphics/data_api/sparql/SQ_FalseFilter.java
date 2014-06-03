/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

public class SQ_FalseFilter implements SQ_WhereElement {

	public static final SQ_FalseFilter value = new SQ_FalseFilter();
	
	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent).append("FILTER(false)").append(SQ.nl);
	}
	
}