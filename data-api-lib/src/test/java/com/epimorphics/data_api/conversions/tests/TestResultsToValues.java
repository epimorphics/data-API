/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.conversions.ResultsToValues;
import com.epimorphics.data_api.conversions.Row;
import com.epimorphics.data_api.conversions.ResultValue;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.results.tests.TestTranslateQuerySolution;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestResultsToValues {
	
	static final Model m = ModelFactory.createDefaultModel();
	
	static final Node itemA = m.createResource( "eh:/A" ).asNode();
	static final Node itemB = m.createResource( "eh:/B" ).asNode();
	
	static final Node value1 = m.createLiteral( "one" ).asNode();
	static final Node value2 = m.createLiteral( "two" ).asNode();
	static final Node value3 = m.createLiteral( "three" ).asNode();
	
	static final ResultValue itemA_value = ResultValue.string(itemA.getURI());
	static final ResultValue itemB_value = ResultValue.string(itemB.getURI());
	
	static final ResultValue value1_value = ResultValue.string("one");
	static final ResultValue value2_value = ResultValue.string("two");
	static final ResultValue value3_value = ResultValue.string("three");
	
	QuerySolution A = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_multiple", value1);
	QuerySolution B = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_multiple", value2);
	QuerySolution C = new TestTranslateQuerySolution.LocalQuerySolution("item", itemB, "pre_multiple", value3);

	@Test public void testSimpleSolutions() {
		List<QuerySolution> x = BunchLib.list(A, C);
		
		Aspect multiple = new TestAspects.MockAspect( "eh:/aspect/multiple" );
		
		List<Aspect> aspects = BunchLib.list( multiple );
				
		List<Row> rows = ResultsToValues.convert(aspects, x);
				
		Row expected_1 = new Row()		
			.put("@id", itemA_value )				
			.put("pre:multiple", value1_value)
			;
				
		Row expected_2 = new Row()
			.put("@id", itemB_value )
			.put("pre:multiple", value3_value)
			;
		
		assertEquals(BunchLib.list(expected_1, expected_2), rows);
	}
	
	@Test public void testOptionalSolutions() {
		
		QuerySolution X = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_optional", value1);
		QuerySolution Y = new TestTranslateQuerySolution.LocalQuerySolution("item", itemB);
		
		List<QuerySolution> x = BunchLib.list(X, Y);
		
		Aspect multiple = new TestAspects.MockAspect( "eh:/aspect/optional" ).setIsOptional(true);
		
		List<Aspect> aspects = BunchLib.list( multiple );
				
		List<Row> rows = ResultsToValues.convert(aspects, x);
				
		Row expected_1 = new Row()		
			.put("@id", itemA_value )				
			.put("pre:optional", ResultValue.array(BunchLib.list(value1_value)))
			;
				
		Row expected_2 = new Row()
			.put("@id", itemB_value )
			.put("pre:optional", ResultValue.array(new ArrayList<ResultValue>()))
			;
				
		assertEquals(BunchLib.list(expected_1, expected_2), rows);
	}
	
	@Test public void testMultipleSolutions() {
		List<QuerySolution> x = BunchLib.list(A, B, C);
		
		Aspect multiple = new TestAspects.MockAspect( "eh:/aspect/multiple" ).setIsMultiValued(true);
		List<Aspect> aspects = BunchLib.list( multiple );
				
		List<Row> rows = ResultsToValues.convert(aspects, x);
				
		Row expected_1 = new Row()		
			.put("@id", itemA_value )				
			.put("pre:multiple", ResultValue.array(BunchLib.list(value1_value, value2_value)) )
			;
				
		Row expected_2 = new Row()
			.put("@id", itemB_value )
			.put("pre:multiple", ResultValue.array(BunchLib.list(value3_value)) )
			;
				
		assertEquals(BunchLib.list(expected_1, expected_2), rows);
	}

	//
	// tests for aspects that are both multiple and optional
	//
	
	final QuerySolution A0 = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA);
	final QuerySolution A1 = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_zom", value1);
	final QuerySolution A2 = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_zom", value2);
	final QuerySolution A3 = new TestTranslateQuerySolution.LocalQuerySolution("item", itemA, "pre_zom", value3);
	
	final Aspect zom = new TestAspects.MockAspect( "eh:/aspect/zom" )
		.setIsMultiValued(true)
		.setIsOptional(true)
		;

	final List<Aspect> zom_aspects = BunchLib.list( zom );
	
	final ResultValue noElements = ResultValue.array(new ArrayList<ResultValue>());

	@Test public void testZomZeroSolutions() {
		List<QuerySolution> x = BunchLib.list(A0);
		List<Row> rows = ResultsToValues.convert(zom_aspects, x);
				
		Row expected_1 = new Row()		
			.put("@id", itemA_value )				
			.put("pre:zom", noElements )
			;
				
		assertEquals(BunchLib.list(expected_1), rows);
	}
	
	@Test public void testZomOneSolution() {
		List<QuerySolution> x = BunchLib.list(A1);
		List<Row> rows = ResultsToValues.convert(zom_aspects, x);
				
		Row expected_1 = new Row()		
			.put("@id", itemA_value )				
			.put("pre:zom", array(value1_value) )
			;
				
		assertEquals(BunchLib.list(expected_1), rows);
	}
	
	@Test public void testZomTwoGivenSolutions() {
		List<QuerySolution> x = BunchLib.list(A1, A2);
		List<Row> rows = ResultsToValues.convert(zom_aspects, x);
				
		Row expected_1 = new Row()		
			.put("@id", itemA_value )				
			.put("pre:zom", array(value1_value, value2_value) )
			;
				
		assertEquals(BunchLib.list(expected_1), rows);
	}
	
	@Test public void testZomTwoSolutionsAndAnOmission() {
		List<QuerySolution> x = BunchLib.list(A0, A1, A2);
		List<Row> rows = ResultsToValues.convert(zom_aspects, x);
				
		Row expected_1 = new Row()		
			.put("@id", itemA_value )				
			.put("pre:zom", array(value1_value, value2_value) )
			;
				
		assertEquals(BunchLib.list(expected_1), rows);
	}
	
	@Test public void testZomTwoSolutionsAndAnOmissionReordered() {
		List<QuerySolution> x = BunchLib.list(A1, A0, A2);
		List<Row> rows = ResultsToValues.convert(zom_aspects, x);
				
		Row expected_1 = new Row()		
			.put("@id", itemA_value )				
			.put("pre:zom", array(value1_value, value2_value) )
			;
				
		assertEquals(BunchLib.list(expected_1), rows);
	}

	private ResultValue array(ResultValue... values) {
		return ResultValue.array(Arrays.asList(values));
	}
	

}
