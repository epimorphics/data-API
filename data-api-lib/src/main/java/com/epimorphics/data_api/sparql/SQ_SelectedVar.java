/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

public class SQ_SelectedVar {

	public final SQ_Variable var;
	public final boolean needsDistinct;
	
	public SQ_SelectedVar(SQ_Variable var, boolean needsDistinct) {
		this.var = var;
		this.needsDistinct = needsDistinct;
	}
}