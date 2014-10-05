/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.unpacking.tests;

import static org.junit.Assert.*;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.Switches;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.parse_data_query.tests.Setup;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.SQ_Node;
import com.epimorphics.data_api.sparql.SQ_Resource;
import com.epimorphics.data_api.sparql.SQ_Triple;
import com.epimorphics.data_api.sparql.SQ_Variable;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestUnpacking {

	static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("space", "dsapi:space/")
		.lock()
		;

	/**
	    Make a dataset containing the specified items. Each item
	    is of the form [?]L[=Path]. L is the local name of the
	    item with prefix "space". If "?" is present the item is
	    optional. If =Path is present then Path is the property
	    path of the item, a series of local names separated by
	    "/"; if absent it is as though it were L.
	*/
	static final API_Dataset makeDataset(String... items) {
		API_Dataset ds = new API_Dataset(Setup.pseudoRoot(pm), null);
		for (String item: items) {
			String [] parts = item.split("=");
			Aspect a = makeAspect(parts[0]);
			if (parts.length > 1) {
				a.setPropertyPath( makePath(parts[1]) );
			}
			ds.add(a);
		}
		return ds;
	}
	
	private static String makePath(String slashed) {
		StringBuilder result = new StringBuilder();
		String slash = "";
		for (String element: slashed.split("/")) {
			result.append(slash).append("space:").append(element);
			slash = "/";
		}
		return result.toString();
	}

	// make an aspect with the given local name
	// if it starts with "?", strip the "?" from the name
	// and make the aspect optional.
	private static Aspect makeAspect(String d) {
		boolean optional = d.startsWith("?");
		if (optional) d = d.substring(1);
		Aspect a = new Aspect(pm, "space:" + d);
		a.setIsOptional(optional);
		return a;
	}

	@Test public void testEmptyQueryWithPropertyPathWithSharing() {
		String query = makeQuery(makeDataset("A", "B=A/D"), "{}");
		denyContains("OPTIONAL", query);
		assertContains("?item space:A ?space_A  .", query);
		assertContains("?space_A space:D ?space_B  .", query);
	}

	@Test public void testEmptyQueryWithPropertyPathWithoutSharing() {
		String query = makeQuery(makeDataset("A", "B=C/D"), "{}");
//		System.err.println(">>\n" + query);
		denyContains("OPTIONAL", query);
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContainsOnce("?item space:C ?space_C  .", query);
		assertContainsOnce("?space_C space:D ?space_B  .", query);
	}

	@Test public void testEmptyQueryWithOptionalPropertyPathWithoutSharing() {
		String query = makeQuery(makeDataset("A", "?B=C/D"), "{}");
		// System.err.println(">>\n" + query);
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContainsOnce("?item space:C ?space_C  .", query);
		assertContainsOnce("?space_C space:D ?space_B  .", query);
		assertContainsOnce("OPTIONAL { ?item space:C ?space_C  . ?space_C space:D ?space_B  . }", query);
	}

	@Test public void testEmptyQueryWithOptionalPropertyPath() {
		String query = makeQuery(makeDataset("A", "?B=A/D"), "{}");
		assertContains("?item space:A ?space_A  .", query);
		assertContains("OPTIONAL { ?space_A space:D ?space_B  . }", query);
	}

	@Test public void testEmptyQueryWithMultiplePropertyPaths() {
		String query = makeQuery(makeDataset("A", "B=A/D", "C=A/E"), "{}");
		denyContains("OPTIONAL", query);
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContains("?space_A space:D ?space_B  .", query);
		assertContains("?space_A space:E ?space_C  .", query);
	}

	@Test public void testQueryWithNonEQFilterOnRequiredPlainAspect() {
		String query = makeQuery(makeDataset("A", "B=A/D"), "{'space:A': {'@lt': 17}}");
		// System.err.println(">>\n" + query);
		denyContains("OPTIONAL", query);
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContains("?space_A space:D ?space_B  .", query);
		assertContains("FILTER(?space_A  < 17)", query);
	}	

	@Test public void testQueryWithNonEQFilterOnRequiredPathedAspect() {
		String query = makeQuery(makeDataset("A", "B=A/D"), "{'space:B': {'@lt': 17}}");
//		System.err.println(">>\n" + query);
		denyContains("OPTIONAL", query);
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContainsOnce("?space_A space:D ?space_B  .", query);
		assertContains("FILTER(?space_B  < 17)", query);
	}	

	@Test public void testQueryWithEQFilterOnRequiredPathAspect() {
		if (Switches.dontTest) return;
		String query = makeQuery(makeDataset("A", "B=A/D"), "{'space:A': {'@eq': 17}}");
		System.err.println(">>\n" + query);
		denyContains("OPTIONAL", query);
		denyContains("FILTER", query);
		denyContains("?item space:A ?space_A  .", query);
	//
		assertContainsOnce("BIND(17 AS ?space_A )", query);
		assertContainsOnce("?item space:A 17 .", query);
		assertContainsOnce("?space_A space:D ?space_B  .", query);
	}

	@Test public void testQueryWithEQFilterOnRequiredPathedAspect() {
		String query = makeQuery(makeDataset("A", "B=A/D"), "{'space:B': {'@eq': 17}}");
//		System.err.println(">>\n" + query);
		denyContains("OPTIONAL", query);
		denyContains("FILTER", query);
	//
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContainsOnce("?space_A space:D 17 .", query);
		assertContainsOnce("BIND(17 AS ?space_B )", query);
	}

	@Test public void testQueryWithEQFilterOnOptionalPathedAspect() {
		String query = makeQuery(makeDataset("A", "?B=A/D"), "{'space:B': {'@eq': 17}}");
//		System.err.println(">>\n" + query);
		denyContains("OPTIONAL", query);
		denyContains("FILTER", query);
	//
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContainsOnce("?space_A space:D 17 .", query);
		assertContainsOnce("BIND(17 AS ?space_B )", query);
	}

	@Test public void testQueryWithNonEQFilterOnRequiredLongerPathedAspect() {
		String query = makeQuery(makeDataset("A", "B=A/C/D"), "{'space:B': {'@lt': 17}}");
		// System.err.println(">>\n" + query);
		denyContains("OPTIONAL", query);
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContainsOnce("?space_A space:C ?space_A__space_C  .", query);
		assertContainsOnce("?space_A__space_C space:D ?space_B  .", query);
		assertContains("FILTER(?space_B  < 17)", query);
	}	

	@Test public void testQueryWithMultipleOptionalPathsAndSharingWithPlainTerm() {
		String query = makeQuery(makeDataset("A", "?B=A/R", "?C=A/S"), "{}");
		// System.err.println(">>\n" + query);
		denyContains("FILTER", query);
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContainsOnce("OPTIONAL { ?space_A space:R ?space_B  . }", query);
		assertContainsOnce("OPTIONAL { ?space_A space:S ?space_C  . }", query);
	}	

	@Test public void testQueryWithNonEQFilterOnOptionalPathedAspect() {
		String query = makeQuery(makeDataset("A", "?B=A/C/D"), "{'space:B': {'@lt': 17}}");
		// System.err.println(">>\n" + query);
		denyContains("OPTIONAL", query);
		assertContainsOnce("?item space:A ?space_A  .", query);
		assertContainsOnce("?space_A space:C ?space_A__space_C  .", query);
		assertContainsOnce("?space_A__space_C space:D ?space_B  .", query);
		assertContains("FILTER(?space_B  < 17)", query);
	}	

	private String makeQuery(API_Dataset ds, String incoming) {
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		if (!p.isOK()) fail(p.getProblemStrings());
		String sparql = q.toSparql(p, ds);
		System.err.println(">> SPARQL:\n" + sparql);
        try { QueryFactory.create(sparql); }
        catch (Exception e) { fail("Bad generated SPARQL:\n" + sparql + "\n" + e.getMessage()); }
		return sparql;
	}
	
	// TODO these below move to SQ testing when ready
	@Test public void testResourceEquality() {
		SQ_Node S = new SQ_Resource("eh:/S");
		SQ_Node P = new SQ_Resource("eh:/P");
		assertDiffer(S, P);
		assertEquals(S, new SQ_Resource("eh:/S"));
		assertEquals(P, new SQ_Resource("eh:/P"));
	}
	
	@Test public void testTripleEquality() {
		SQ_Node S = new SQ_Resource("eh:/S");
		SQ_Node P = new SQ_Resource("eh:/P");
		SQ_Node O = new SQ_Resource("eh:/O");
		SQ_Triple a = new SQ_Triple(S, P, O);
		assertEquals(a, new SQ_Triple(S, P, O));
		assertDiffer(a, new SQ_Triple(O, P, S));
	}
	
	@Test public void testVariableEquality() {
		SQ_Variable v = new SQ_Variable("X");
		assertDiffer(v, new SQ_Variable("Y"));
		assertEquals(v, new SQ_Variable("X"));
	}

	private void assertDiffer(Object a, Object b) {
		if (a.equals(b)) 
			fail("expected values to be different but both " + a);
	}

	private void assertContainsOnce(String wanted, String subject) {
		int first = subject.indexOf(wanted);
		if (first < 0) {
			fail("the fragment `" + wanted + "` did not appear in the subject:\n" + subject);
		} else {
			int second = subject.indexOf(wanted, first + wanted.length());
			if (second > -1) 
				fail("the fragment `" + wanted + "` appeared more than once in the subject:\n" + subject);
		}
	}

	private void assertContains(String wanted, String subject) {
		if (subject.contains(wanted)) return;
		fail("the fragment `" + wanted + "` did not appear in the subject:\n" + subject);
	}

	private void denyContains(String unwanted, String subject) {
		if (subject.contains(unwanted))
			fail("the unwanted fragment `" + unwanted + "` appeared in the subject:\n" + subject);
	}

}
