/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.List;
import java.util.Set;

// TODO add operator slot of some kind (NOT a String, 
// maybe the existing Operator
public interface SQ_Expr {
	
	public void toSparqlExpr(StringBuilder sb);
	
	public List<SQ_Expr> operands();
	
	public void updateVars(Set<String> varNames);
}