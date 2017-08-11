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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import static org.junit.Assert.fail;

public class TestCorrectOrderOfSubclauses {
	
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
	
	static final Resource CType = ResourceFactory.createResource("eh:/CTYpe");
	
	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null)
		.add(A)
		.add(B)
		.add(C.setRangeType(CType))
		.add(D.setIsOptional(true))
		;
	
	static { ds.setIsLiteralType(CType); }

	@Test public void requireFilterAppearsAfterItem() {
		assertGeneratesInOrder
			( query(aspect("B", "lt", "17"))
			, item("A")
			, filter("B", "<", "17")
			);
	}

	@Test public void requireLiteralAppearsBeforeItem() {
		assertGeneratesInOrder
			( query(aspect("B", "eq", id("X")))
			, "?item pre:B <eh:/X>"
			, item("A") 
			);
	}

	@Test public void requireLiteralAppearsBeforeOptional() {
		assertGeneratesInOrder
			( query(aspect("A", "eq", id("X")))
			, "?item pre:A <eh:/X>"
			, "OPTIONAL"
			, item("D") 
			);
	}

	@Test public void requireOrderIsLiteralThenItemThenFilterThenOptionalThenBind() {
		assertGeneratesInOrder
			( query(aspect("A", "eq", id("X")), aspect("B", "lt", "17"))
			, "?item pre:A <eh:/X>"
			, item("B")
			, filter("B", "<", "17")
			, "OPTIONAL"
			, "BIND(<eh:/X> AS ?pre_A)"
			);
	}
	
	@Test public void requireOrderIsSearchThenLiteralThenOptionalThenBind() {
		assertGeneratesInOrder
			( query(aspect("A", "eq", id("X")), aspect("B", "search", "'string'"))
			, "?pre_B text:query \"string\""
			, "?item pre:B ?pre_B"
			, "?item pre:A <eh:/X>"
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
		DataQuery dq = DataQueryParser.Do(p, ds, jo);
		if (!p.isOK()) fail("Could not construct data query: " + p.getProblemStrings());
	//
		String sparqlString = dq.toSparql(p, ds);
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

	/**
		query(parts) returns a DSAPI query constructed by making
		a single JSON object from the comma-separated parts.
	*/
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
