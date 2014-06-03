/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

public interface SQ_WhereElement {

	public void toSparqlStatement(StringBuilder sb, String indent);
	
}