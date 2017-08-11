/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.test_support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.epimorphics.data_api.reporting.Problems;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;

public class Asserts {

	public static void assertInsensitiveContains(String expected, String s) {
		if (!s.toLowerCase().contains(expected.toLowerCase()))
			fail("'" + expected + "' was not present in '" + s + "'");		
	}

	public static void assertContains(String wanted, String subject) {
		if (subject.contains(wanted)) return;
		fail("the fragment `" + wanted + "` did not appear in the subject:\n" + subject);
	}

	public static void assertContainsOnce(String wanted, String subject) {
		int first = subject.indexOf(wanted);
		if (first < 0) {
			fail("the fragment `" + wanted + "` did not appear in the subject:\n" + subject);
		} else {
			int second = subject.indexOf(wanted, first + wanted.length());
			if (second > -1) 
				fail("the fragment `" + wanted + "` appeared more than once in the subject:\n" + subject);
		}
	}

	public static void denyContains(String unwanted, String subject) {
		if (subject.contains(unwanted))
			fail("the unwanted fragment `" + unwanted + "` appeared in the subject:\n" + subject);
	}

	public static void assertNoProblems(String tag, Problems p) {
		if (p.size() > 0) fail(tag + ": " + p.getProblemStrings());
	}

	/**
	    Assert that the SPARQL query toTest is the same as expected,
	    by comparing their parsed forms. If either query is illegal,
	    report the parse failure with fail rather than letting the
	    QueryParseException float up.
	*/
	public static void assertSameSelect(String expected, String toTest) {
		Query t = null, e = null;
		try {
			if (expected != null) e = QueryFactory.create(expected);
			if (toTest != null) t = QueryFactory.create(toTest);
			
//			System.err.println(">> EXPECTED:\n" + e.toString() );
//			System.err.println(">> OBTAINED:\n" + t.toString() );
			
			assertEquals(e, t);
		} catch (QueryParseException q) {
			if (e == null) {
				fail( "parse failure: " + q.getMessage() + "\n" + expected );
			}
			if (t == null) {
				fail( "parse failure: " + q.getMessage() + "\n" + toTest );
			}
		}
	}

	public static void assertDiffer(Object expect, Object o) {
		if (expect.equals(o)) fail("expected something different from " + expect);
	}

}
