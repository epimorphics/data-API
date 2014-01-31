/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.shared.PrefixMapping;

public class Restriction {
	
	public static final List<Restriction> NONE = new ArrayList<Restriction>();

	final Term predicate;
	final Term object;
	
	public Restriction(PrefixMapping pm, String predicate, String object) {
		this.predicate = Term.URI(pm.expandPrefix(predicate));
		this.object = Term.URI(pm.expandPrefix(object));
	}
	
	public String asSparqlTriple(Term subject) {
		return subject.asSparqlTerm() + " " + predicate.asSparqlTerm() + " " + object.asSparqlTerm();
	}


}
