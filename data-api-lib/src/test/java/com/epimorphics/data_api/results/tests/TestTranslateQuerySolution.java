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

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.conversions.Compactions;
import com.epimorphics.data_api.conversions.ResultsToRows;
import com.epimorphics.data_api.conversions.Row;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.libs.BunchLib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class TestTranslateQuerySolution {

	Node A = NodeFactory.createURI("eh:/some-A");
	Node B = NodeFactory.createLiteral("hello, world");
	
	Aspect aspectA = new TestAspects.MockAspect("eh:/aspect/a");
	Aspect aspectB = new TestAspects.MockAspect("eh:/aspect/b");
	
	@Test public void testTranslate() {
		QuerySolution qs = new LocalQuerySolution("pre_a", A, "pre_b", B);
		List<Aspect> aspects = BunchLib.list(aspectA, aspectB);
		
//		System.err.println( ">> aspects: " + aspects );
		
		ResultsToRows rr = new ResultsToRows(aspects, Compactions.None);
		Row js = rr.solutionToRow(qs);
		
		Row expected = new Row()
			.put("pre:a", Term.fromNode(Compactions.None, A))
			.put("pre:b", Term.fromNode(Compactions.None, B))
			;
		
		assertEquals( expected, js );		
	}
	
	public static class LocalQuerySolution implements QuerySolution {

		final Map<String, RDFNode> results = new HashMap<String, RDFNode>();
		
		static final Model model = ModelFactory.createDefaultModel();
		
		public LocalQuerySolution(Object ...bindings) {
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
