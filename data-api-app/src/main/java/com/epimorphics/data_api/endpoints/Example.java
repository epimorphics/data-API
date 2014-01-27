/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import com.epimorphics.data_api.aspects.Aspects;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Example {
	
	final PrefixMapping pm;
	final Aspects aspects;
	final Model model;
	
	Example(PrefixMapping pm, Aspects aspects, Model model) {
		this.pm = pm;
		this.aspects = aspects;
		this.model = model;
	}
	
}