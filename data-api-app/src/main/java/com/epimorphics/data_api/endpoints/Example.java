/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import java.util.List;
import java.util.Set;

import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.data.impl.BaseSparqlSource;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.Restriction;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.Map1;

public class Example {
	
	final PrefixMapping pm;
	final Aspects aspects;
	final Model model;
	final SparqlSource source;
	final List<Restriction> restrictions;
	
	Example(PrefixMapping pm, Aspects aspects, List<Restriction> restrictions, Model model) {
		this.pm = pm;
		this.aspects = aspects;
		this.restrictions = restrictions;
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
	
	static final Map1<RDFNode, String> getType = new Map1<RDFNode, String>() {
	
		@Override public String map1(RDFNode o) {
			if (o.isLiteral()) return o.asNode().getLiteralDatatypeURI();
			return null;
		}
	
	};
	
	static Resource findRangeType(Model m, Property p) {
		Set<String> types = m
			.listStatements(null, p, (RDFNode) null)
			.mapWith(Statement.Util.getObject)
			.mapWith(Example.getType).toSet()
			;
		return types.size() == 1 ? m.createResource(types.iterator().next()) : null;
	}

	public List<Restriction> restrictions() {
		return restrictions;
	}
}