/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Set;

public interface SQ_WhereElement {

	public void updateVars(Set<String> varNames);
	
	public void toSparqlStatement(StringBuilder sb, String indent);
	
}