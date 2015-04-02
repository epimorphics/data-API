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

	@Test public void testA() {
		assertGeneratesInOrder
			( query(aspect("B", "lt", "17"))
			, item("A")
			, filter("B", "<", "17")
			);
	}

	@Test public void testB() {
		assertGeneratesInOrder
			( query(aspect("A", "eq", id("X")))
			, "?item pre:A <eh:/X>"
			, item("B") 
			);
	}

	@Test public void testC() {
		assertGeneratesInOrder
			( query(aspect("A", "eq", id("X")))
			, "?item pre:A <eh:/X>"
			, "OPTIONAL"
			, item("D") 
			);
	}

	@Test public void testD() {
		assertGeneratesInOrder
			( query(aspect("A", "eq", id("X")), aspect("B", "lt", "17"))
			, "?item pre:A <eh:/X>"
			, item("B")
			, filter("B", "<", "17")
			, "OPTIONAL"
			, "BIND(<eh:/X> AS ?pre_A)"
			);
	}
	
	private String id(String name) {
		return "{'@id': 'eh:/" + name + "'}";
	}

	private String item(String name) {
		return "?item pre:" + name + " ?pre_" + name;
	}
	
	private String aspect(String name, String op, String value) {
		return "'pre:" + name + "': {'@" + op + "': " + value + "}";
	}
	
	private String filter(String name, String op, String value) {
		return "FILTER(?pre_" + name + " " + op + " " + value + ")";
	}
	
	private void assertGeneratesInOrder(String queryString, String... fragments) {
		Problems p = new Problems();
		JsonObject jo = JSON.parse(queryString);
		DataQuery dq = DataQueryParser.Do(p, dsAB, jo);
		if (!p.isOK()) fail("Could not construct data query: " + p.getProblemStrings());
	//
		String sparqlString = dq.toSparql(p, dsAB);
		if (!p.isOK()) fail("Could not construct SPARQL query: " + p.getProblemStrings());
		
//		System.err.println(">> QUERY:\n" + sparqlString);
	//
		String scan = sparqlString;
		for (int fragIndex = 0; fragIndex < fragments.length; fragIndex += 1) {
			String f = fragments[fragIndex];
			int i = scan.indexOf(f);
			if (i < 0) {
				String previous = (fragIndex == 0 ? "WHERE" : fragments[fragIndex - 1]);
				String next = (fragIndex == fragments.length - 1 ? "the end of the query" : fragments[fragIndex + 1]);
				fail
					( "in the query generated from " + queryString + ","
					+ "\nthe clause " + f
					+ "\nshould appear after " + previous
					+ "\nshould appear before  " + next
					+ "\nin the generated query\n" 
					+ sparqlString
					)
					;
			} else {
				scan = scan.substring(i + f.length());
			}
		}
	}

	private String query(String... parts) {		
		String result = "{", comma = "";
		for (String p: parts) {
			result = result + comma + p;
			comma = ", ";			
		}
		result += "}";
		return result;
	}

}
