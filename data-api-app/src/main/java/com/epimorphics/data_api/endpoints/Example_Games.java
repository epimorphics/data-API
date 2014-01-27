/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.Shortname;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Example_Games {

	public static Example configureGames(Model m) {
		PrefixMapping pm = PrefixMapping.Factory
				.create()
				.setNsPrefixes(PrefixMapping.Extended)
				.setNsPrefixes(m)
				.lock();
	
		Set<Property> predicates = m.listStatements().mapWith(Statement.Util.getPredicate).toSet();
	
		Set<String> optional = new HashSet<String>();
		Set<String> multiple = new HashSet<String>();
		
		optional.add("egc:playTimeMinutes");
		
		multiple.add("egc:players");
		multiple.add("rdfs:label");
		
		Set<String> allowed = new HashSet<String>();
		
		allowed.add("rdfs:label");
		allowed.add("rdf:type");
		allowed.add("egc:players");
		allowed.add("egc:pubYear");
		allowed.add("egc:playTimeMinutes");
		
		Aspects aspects = new Aspects();
		
		for (Property p: predicates) {
			Resource rangeType = Placeholder.findRangeType(m, p);
			String ID = p.getURI();
			String sn = pm.shortForm(ID);
			if (allowed.contains(sn)) {
				Aspect a = new Aspect(ID, new Shortname(pm, sn));
				if (optional.contains(sn)) a.setIsOptional(true);
				if (multiple.contains(sn)) a.setIsMultiValued(true);
				if (rangeType != null) a.setRangeType(rangeType);
				aspects.include(a);
			}
		}
	
		return new Example( pm, aspects, m );
	}

}
