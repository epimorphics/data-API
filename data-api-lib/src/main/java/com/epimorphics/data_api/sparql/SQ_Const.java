/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.Collections;
import java.util.List;

public class SQ_Const {
	public static final SQ_Variable item = new SQ_Variable("item");
	
	// Will be used in a context where text: is defined.
	public static final SQ_Node textQuery = new SQ_Resource("text:query"); 
	
	public static final List<SQ_Expr> NONE = Collections.<SQ_Expr>emptyList();
	
}