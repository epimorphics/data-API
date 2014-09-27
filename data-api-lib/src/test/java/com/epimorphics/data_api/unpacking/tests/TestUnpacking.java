/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.unpacking.tests;

import static org.junit.Assert.*;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.parse_data_query.tests.Setup;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.SQ_Node;
import com.epimorphics.data_api.sparql.SQ_Resource;
import com.epimorphics.data_api.sparql.SQ_Triple;
import com.epimorphics.data_api.sparql.SQ_Variable;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestUnpacking {

	static final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("space", "dsapi:space/")
		.lock()
		;

	static final API_Dataset ds = createDataset();

	private static API_Dataset createDataset() {
		API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null);
		Aspect A = new Aspect(pm, "space:A");
		Aspect B = new Aspect(pm, "space:B").setPropertyPath("space:A/space:D");
		// Aspect C = new Aspect(pm, "space:C");
		ds.add(A).add(B); // .add(C);
		return ds;
	}
	
	static final API_Dataset makeDataset(String... items) {
		API_Dataset ds = new API_Dataset(Setup.pseudoRoot(), null);
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

	private static Aspect makeAspect(String d) {
		return new Aspect(pm, "space:" + d);
	}

	@Test public void testEmptyQuery() {

		String query = makeQuery(makeDataset("A", "B=A/D"), "{}");
		
		assertContains("?item space:A ?space_A  .", query);
		assertContains("?space_A space:D ?space_B  .", query);
	}

	private String makeQuery(API_Dataset ds, String incoming) {
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery q = DataQueryParser.Do(p, ds, jo);
		return q.toSparql(p, ds);
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

	private void assertContains(String wanted, String subject) {
		if (subject.contains(wanted)) return;
		fail("the fragment `" + wanted + "` did not appear in the subject:\n" + subject);
	}

}
