/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.results.tests;

import java.util.Iterator;

import org.junit.Test;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestTranslateQuerySolution {
	
	@Test public void testMe() {
		
		QuerySolution qs = new LocalQuerySolution();
		
		
		
	}
	
	static class LocalQuerySolution implements QuerySolution {

		LocalQuerySolution() {
			
		}
		
		@Override public RDFNode get(String varName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override public Resource getResource(String varName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override public Literal getLiteral(String varName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override public boolean contains(String varName) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override public Iterator<String> varNames() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
