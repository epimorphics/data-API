/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Constraint;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Filter;
import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;

public class TestConstructsMatchFilters {
	
	static final API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null);
	static final Aspect local = new Aspect(ds.getPrefixes(), "pre:local");
	
	{ ds.add(local); }
	
	static final Operator matchesOp = Operator.lookup("matches");
	
	@Test public void testBasicMatchesFilter() {
		String incoming = "{'pre:local': {'@matches': 'pattern 1'}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("basic matches filter fails", p);
		Constraint expected = new Filter(local, new Range(matchesOp, BunchLib.list(Term.string("pattern 1"))));
		assertEquals(expected, q.constraint());
	}
	
	@Test public void testBasicMatchesFilterWithFlags() {
		String incoming = "{'pre:local': {'@matches': ['pattern 1', 'flaggy']}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("basic matches filter fails", p);
		Constraint expected = new Filter(local, new Range(matchesOp, BunchLib.list(Term.string("pattern 1"), Term.string("flaggy"))));
		assertEquals(expected, q.constraint());
	}		
	
	@Test public void testValueMatchesFilter() {
		String incoming = "{'pre:local': {'@matches': {'@value': 'pattern two'}}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("basic matches filter fails", p);
		Constraint expected = new Filter(local, new Range(matchesOp, BunchLib.list(Term.string("pattern two"))));
		assertEquals(expected, q.constraint());
	}
	
	@Test public void testValueAndFlagsMatchesFilter() {
		String incoming = "{'pre:local': {'@matches': {'@value': 'pattern.3', '@flags': 'flaggy'}}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("basic matches filter fails", p);
		Constraint expected = new Filter(local, new Range(matchesOp, BunchLib.list(Term.string("pattern.3"), Term.string("flaggy"))));
		assertEquals(expected, q.constraint());
	}
	
	@Test public void testInsensitiveFilter() {
		String incoming = "{'pre:local': {'@matches': {'@case-insensitive-value': 'pattern.3'}}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);		
		Asserts.assertNoProblems("basic matches filter fails", p);
		Constraint expected = new Filter(local, new Range(matchesOp, BunchLib.list(Term.string("pattern.3"), Term.string("i"))));
		assertEquals(expected, q.constraint());
	}
	
	@Test public void testReportsMissingValue() {
		String incoming = "{'pre:local': {'@matches': {'@flags': 'flaggy'}}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertTrue("should detect missing @value in @matches object argument", p.size() > 0);
		Asserts.assertContains("one of @value and @case-insensitive-value should be specified", p.getProblemStrings());
		Asserts.assertContains("{ \"@flags\" : \"flaggy\" }", p.getProblemStrings());
	}
	
	@Test public void testReportsBothValue() {
		String incoming = "{'pre:local': {'@matches': {'@value': 'A', '@case-insensitive-value': 'B', '@flags': 'flaggy'}}}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		assertTrue("should detect missing @value in @matches object argument", p.size() > 0);
		Asserts.assertContains("exactly one of @value and @case-insensitive-value should be specified", p.getProblemStrings());
		Asserts.assertContains("\"@flags\" : \"flaggy\"", p.getProblemStrings());
	}
	
}
