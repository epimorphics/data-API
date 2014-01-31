/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import java.util.Set;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.Restriction;
import com.epimorphics.data_api.data_queries.Shortname;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Example_Legacy {

	public static Example configureLegacy(Model m) {
		
		PrefixMapping pm = PrefixMapping.Factory
			.create()
			.setNsPrefixes(PrefixMapping.Extended)
			.setNsPrefixes(m)
			.setNsPrefix( "wbc", "http://environment.data.gov.uk/def/waterbody-classification/" )
			.setNsPrefix( "qb", "http://purl.org/linked-data/cube#" )
			.lock();
		
		Set<Property> predicates = m.listStatements().mapWith(Statement.Util.getPredicate).toSet();
		
		Aspects aspects = new Aspects();
		
		for (Property p: predicates) {
			Resource rangeType = Example.findRangeType(m, p);
			String ID = p.getURI();
			String sn = pm.shortForm(ID);
			Aspect a = new Aspect(ID, new Shortname(pm, sn));
			if (rangeType != null) a.setRangeType(rangeType);
			aspects.include(a);
		}
		
		return new Example(pm, aspects, Restriction.NONE, m);
	}

}
