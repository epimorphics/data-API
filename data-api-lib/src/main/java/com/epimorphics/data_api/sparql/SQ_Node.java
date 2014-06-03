/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.List;


public abstract class SQ_Node implements SQ_Expr {

	@Override public List<SQ_Expr> operands() {
		return SQ_Const.NONE;
	}
	
}