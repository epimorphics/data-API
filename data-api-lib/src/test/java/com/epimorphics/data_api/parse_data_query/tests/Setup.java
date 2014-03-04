/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Setup {

	static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("pre", "eh:/prefixPart/" )
		.lock()
		;
	
	/**
	    create a resource that is in a model with the tests's standard
	*/
	public static Resource pseudoRoot() {
		Model m = ModelFactory.createDefaultModel();
		m.setNsPrefixes(pm);
		m.setNsPrefix("skos", SKOS.NS);
		return m.createResource("eh:/pseudoRoot");
	}
	
	public static final Aspect localAspect = new Aspect(pm.expandPrefix("pre:local"), new Shortname(pm, "pre:local"));

}
