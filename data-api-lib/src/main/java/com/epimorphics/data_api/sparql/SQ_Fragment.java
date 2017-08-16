/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Set;

public class SQ_Fragment implements SQ_WhereElement {

	final String content;
	
	public SQ_Fragment(String content) {
		this.content = content;
	}
	
	@Override public void toSparqlStatement(StringBuilder sb, String indent) {
		sb.append(indent).append(content).append(SQ.nl);
	}

	@Override public void updateVars(Set<String> varNames) {
		// we can't know. Can we?
		// TODO
	}
	
}