/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

public interface Compactions {

	public boolean suppressTypes();
	
	public boolean squeezeValues();
	
	Compactions None = new Compactions() {
		
		@Override public boolean suppressTypes() {
			return false;
		}
		
		@Override public boolean squeezeValues() {
			return false;
		}
	};
}
