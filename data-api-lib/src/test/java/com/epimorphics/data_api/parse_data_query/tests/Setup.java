/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.DefaultPrefixes;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Setup {

	public static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("pre", "eh:/prefixPart/" )
		.lock()
		;
	
	/**
	    create a resource that is in a model with the tests's standard
	*/
	public static Resource pseudoRoot() {
		return pseudoRoot(pm);
	}
	
	public static Resource pseudoRoot(PrefixMapping pm) {
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefixes(pm);
		m.setNsPrefixes(DefaultPrefixes.get());
		return m.createResource("eh:/pseudoRoot");
	}
	
	public static final Aspect localAspect = new Aspect(pm, "pre:local");

}
