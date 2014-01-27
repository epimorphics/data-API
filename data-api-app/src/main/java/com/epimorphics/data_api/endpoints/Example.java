/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.data.impl.BaseSparqlSource;
import com.epimorphics.data_api.aspects.Aspects;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Example {
	
	final PrefixMapping pm;
	final Aspects aspects;
	final Model model;
	final SparqlSource source;
	
	Example(PrefixMapping pm, Aspects aspects, Model model) {
		this.pm = pm;
		this.aspects = aspects;
		this.model = model;
		this.source = new ModelSparqlSource(model);
	}
	
	public static class ModelSparqlSource extends BaseSparqlSource {
		
		final Model model;
		
		public ModelSparqlSource(Model model) {
			this.model = model;
		}
    
	    @Override protected QueryExecution start(String queryString) {
	        Query query = QueryFactory.create(queryString) ;
	        QueryExecution qexec = QueryExecutionFactory.create(query, model) ;
	        model.getLock().enterCriticalSection(true);
	        return qexec;
	    }
	    
	    @Override protected void finish(QueryExecution qexec) {
	        qexec.close() ;
	        model.getLock().leaveCriticalSection();
	    }
		
	}
}