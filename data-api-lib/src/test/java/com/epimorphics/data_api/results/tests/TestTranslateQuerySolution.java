/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.results.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.conversions.Convert;
import com.epimorphics.data_api.libs.BunchLib;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestTranslateQuerySolution {

	Node A = NodeFactory.createURI("eh:/some-A");
	Node B = NodeFactory.createLiteral("hello, world");
	
	@Test public void testMe() {
		List<String> vars = BunchLib.list("a", "b");
		QuerySolution qs = new LocalQuerySolution("a", A, "b", B);
		JsonObject js = Convert.toJson(vars, qs);
		JsonObject expected = Convert.objectWith("a", Convert.toJson(A), "b", Convert.toJson(B));
		assertEquals( expected, js );		
	}
	
	static class LocalQuerySolution implements QuerySolution {

		final Map<String, RDFNode> results = new HashMap<String, RDFNode>();
		
		static final Model model = ModelFactory.createDefaultModel();
		
		LocalQuerySolution(Object ...bindings) {
			for (int i = 0; i < bindings.length; i += 2) {
				String key = (String) bindings[i];
				Node value = (Node) bindings[i+1];
				results.put(key, model.asRDFNode(value));
			}
		}
		
		@Override public RDFNode get(String varName) {
			return results.get(varName);
		}

		@Override public Resource getResource(String varName) {
			return (Resource) get(varName);
		}

		@Override public Literal getLiteral(String varName) {
			return (Literal) get(varName);
		}

		@Override public boolean contains(String varName) {
			return results.containsKey(varName);
		}

		@Override public Iterator<String> varNames() {
			return results.keySet().iterator();
		}
		
	}

}