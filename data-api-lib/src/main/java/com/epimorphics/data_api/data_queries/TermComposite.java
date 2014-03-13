/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.json.JSFullWriter;

/**
    Composite objects (as JSON) override the writeElement
    method.
*/
public abstract class TermComposite extends Term {

	final String value;
	
	public TermComposite(String value) {
		this.value = value;
	}
	@Override public void writeElement(JSFullWriter jw) {
		jw.arrayElementProcess();
		writeTo(jw);		
	}
}
