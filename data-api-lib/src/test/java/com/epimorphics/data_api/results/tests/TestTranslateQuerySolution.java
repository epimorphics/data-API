/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.results.tests;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Iterator;

import org.apache.jena.atlas.json.JsonBoolean;
import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Test;

import com.epimorphics.data_api.conversions.Convert;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestTranslateQuerySolution {
	
	@Test public void testMe() {
		
		QuerySolution qs = new LocalQuerySolution();
		
		JsonObject js = toJson(qs);
		
		JsonObject expected = new JsonObject();
		
		// assertEquals( expected, js );
		
		System.err.println( ">> test translate query solution not done yet." );
		
	}
	
	
	private JsonObject toJson(QuerySolution qs) {
		return null;
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
