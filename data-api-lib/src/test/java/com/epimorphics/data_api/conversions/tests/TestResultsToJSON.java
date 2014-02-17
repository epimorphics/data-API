/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.conversions.ResultsToJson;
import com.epimorphics.data_api.conversions.Value;
import com.epimorphics.data_api.conversions.ResultsToJson.Row;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.results.tests.TestTranslateQuerySolution;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestResultsToJSON {
	
	static final Model m = ModelFactory.createDefaultModel();
	
	static final Node itemA = m.createResource( "eh:/A" ).asNode();
	static final Node itemB = m.createResource( "eh:/B" ).asNode();
	
	static final Node value1 = m.createLiteral( "one" ).asNode();
	static final Node value2 = m.createLiteral( "two" ).asNode();
	static final Node value3 = m.createLiteral( "three" ).asNode();
	
	static final Value itemA_value = Value.URI("@id", itemA.getURI());
	static final Value itemB_value = Value.URI("@id", itemB.getURI());
	
	static final Value value1_value = Value.string("A", "one");
	static final Value value2_value = Value.string("B", "two");
	static final Value value3_value = Value.string("C", "three");
	
	@Test public void testMe() {
		QuerySolution A = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_multiple", value1);
		QuerySolution B = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_multiple", value2);
		QuerySolution C = new TestTranslateQuerySolution.LocalQuerySolution("item", itemB, "pre_multiple", value3);
		List<QuerySolution> x = BunchLib.list(A, B, C);
		
		Aspect multiple = new TestAspects.MockAspect( "eh:/aspect/multiple" ).setIsMultiValued(true);
		List<Aspect> aspects = BunchLib.list( multiple );
				
		List<Row> ja = ResultsToJson.convert(aspects, x);
				
		Row expected_1 = new Row();
		
		expected_1.put("item", itemA_value );
				
		expected_1.put("pre:multiple", Value.array(BunchLib.list(value1_value, value2_value)) );
				
		Row expected_2 = new Row();
		expected_2.put("item", itemB_value );
		expected_2.put("pre:multiple", Value.array(BunchLib.list(value3_value)) );
				
		List<Row> expected = BunchLib.list(expected_1, expected_2);
				
		assertEquals(expected, ja);
	}
	
	

}
