/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.scratch;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.parse_data_query.tests.Setup;
import com.epimorphics.data_api.reporting.Problems;

import static org.junit.Assert.fail;

public class TestScratch {
	
	/*
		Tests that requests generate their components in the
		right place in the query WHERE clause:
		
		* any search term at the beginning
		* any grounded ?item triple
		* any ungrounded ?item triple
		* filters
		* optional triples
		* BINDings
		
		So there are six kinds of term and to check the ordering
		we need 36 tests.  We'll set up a loop, easier to manage 
		even if a bit harder to handle when there are failures.
		
	*/
	
	
	static final Aspect A = new TestAspects.MockAspect("eh:/prefixPart/A");
	static final Aspect B = new TestAspects.MockAspect("eh:/prefixPart/B");
	static final Aspect C = new TestAspects.MockAspect("eh:/prefixPart/C");
	static final Aspect D = new TestAspects.MockAspect("eh:/prefixPart/D");
	
	static final API_Dataset dsAB = new API_Dataset(Setup.pseudoRoot(), null)
		.add(A)
		.add(B)
		.add(C)
		.add(D.setIsOptional(true))
		;
	
	String[] A_free = new String [] {"", "", "?item pre:A ?pre_A"};
	String[] B_free = new String [] {"", "", "?item pre:B ?pre_B"};
	String[] C_free = new String [] {"", "", "?item pre:C ?pre_C"};
	String[] D_free = new String [] {"", "", "OPTIONAL { ?item pre:D ?pre_D . }"};

	String[] A_eq_17 = new String[] {"", "'pre:A': {'@eq': 17}", "FILTER(?pre_A = 17)"};
	String[] A_eq_X  = new String[] {"", "'pre:A': {'@eq': {'@id': 'eh:/X'}}", "?item pre:A <eh:/X>"};
	String[] B_lt_17 = new String[] {"", "'pre:B': {'@lt': 17}", "FILTER(?pre_B < 17)"};
	
	@Test public void testSomething() {
		runTest(A_free, B_lt_17);
		runTest(A_free, D_free);
		runTest(A_eq_X, B_free);
		runTest(A_eq_X, D_free);
		runTest(A_eq_X, B_lt_17);
		runTest(B_lt_17, D_free);
		runTest(B_free, A_eq_17);
		
	}

	private void runTest(String[] firstTerm, String[] secondTerm) {
		String queryString = both(firstTerm, secondTerm);		
		Problems p = new Problems();
		JsonObject jo = JSON.parse(queryString);
		DataQuery dq = DataQueryParser.Do(p, dsAB, jo);
		if (!p.isOK()) fail("Could not construct data query: " + p.getProblemStrings());
	//
		String sparqlString = dq.toSparql(p, dsAB);
		if (!p.isOK()) fail("Could not construct SPARQL query: " + p.getProblemStrings());
	//
		System.err.println(">> query:\n" + sparqlString);
		
		if (!matches(sparqlString, firstTerm[2], secondTerm[2])) {
			fail
				( "in the query generated from " + queryString + ","
				+ "\nthe clause " + firstTerm[2]
				+ "\nshould appear before the clause " + secondTerm[2]
				+ "\nin the generated query\n" 
				+ sparqlString
				)
				;
		}
	}

	private boolean matches(String sparqlString, String first, String second) {
		int f = sparqlString.indexOf(first);
		if (f < 0) return false;
		int s = sparqlString.indexOf(second, f + first.length());
		return s >= 0;
	}

	private String both(String[] firstTerm, String[] secondTerm) {
		String F = firstTerm[1], S = secondTerm[1];
		String comma = F.equals("") || S.equals("") ? "" : ", ";
		return "{" + firstTerm[1] + comma + secondTerm[1] + "}";
	}

}
