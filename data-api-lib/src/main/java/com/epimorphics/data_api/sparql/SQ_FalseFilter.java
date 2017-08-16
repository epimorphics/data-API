/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Set;

public class SQ_FalseFilter implements SQ_WhereElement {

	public static final SQ_FalseFilter value = new SQ_FalseFilter();
	
	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent).append("FILTER(false)").append(SQ.nl);
	}

	@Override public void updateVars(Set<String> varNames) {
		// No variables in FILTER(FALSE).
	}
	
}