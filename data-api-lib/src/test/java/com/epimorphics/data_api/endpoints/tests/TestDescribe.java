/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.jena.riot.RDFLanguages;
import org.junit.Test;

import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.data.impl.ModelSparqlSource;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestDescribe {

	static final String preamble = BunchLib.join
		( "@prefix rdf: 	<http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
		, "@prefix rdfs: 	<http://www.w3.org/2000/01/rdf-schema#> ."
		, "@prefix : 		<eh:/namespace#> ."
		);
	
	static final String content = BunchLib.join
		( ":A :P 1"
		, ". :B :Q 2"
		, ". :C :R 3"
		, ". :D :S [:T 4]"
		, "."
		);

	Model contentModel = ModelFactory
		.createDefaultModel()
		.read( new StringReader(preamble + content), null, RDFLanguages.strLangTurtle )
		; 
	
	Model readTurtle(String text) {
		return ModelFactory
			.createDefaultModel()
			.read( new StringReader(preamble + text), null, RDFLanguages.strLangTurtle )
		;
	} 
	
	Model readJSON(String text) {
		return ModelFactory
			.createDefaultModel()
			.read( new StringReader(text), null, RDFLanguages.strLangJSONLD )
		;
	}
	
	Resource r(String leaf) {
		return contentModel.createResource(contentModel.expandPrefix(leaf));
	}

	Resource A = r(":A"), B = r(":B"), C = r(":C"), D = r(":D");
	
	@Test public void testAB() throws WebApplicationException, IOException {			
		List<String> uris = BunchLib.list(A.getURI(), B.getURI());
		Model expected = readTurtle(":A :P 1. :B :Q 2.");
		testExample(uris, expected);
	}	
	
	@Test public void testBC() throws WebApplicationException, IOException {			
		List<String> uris = BunchLib.list(B.getURI(), C.getURI());
		Model expected = readTurtle(":B :Q 2. :C :R 3.");
		testExample(uris, expected);
	}
	
	@Test public void testBCxx() throws WebApplicationException, IOException {			
		List<String> uris = BunchLib.list(D.getURI());
		Model expected = readTurtle(":D :S [:T 4]");
		testExample(uris, expected);
	}

	private void testExample(List<String> uris, Model expected)	throws IOException {
		DSAPIManager d = makeManager();
		SparqlSource s = new ModelSparqlSource(contentModel);
		d.setSource(s);
	//
		Response r = d.datasetDescribeEndpoint("unused", uris);
		
		StreamingOutput so = (StreamingOutput) r.getEntity();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		so.write(bos);
	//
		assertIso( expected, readJSON(bos.toString()) );
	}

	// make a manager that fakes out the dataset
	private DSAPIManager makeManager() {
		Resource config = contentModel.createResource("eh:/config");
		
		final API_Dataset[] ads = new API_Dataset[1];
		
 		DSAPIManager d = new DSAPIManager() {
			
			@Override public API_Dataset getAPI(String name) {
				return ads[0];
			}
		};
		
		final API_Dataset ad = new API_Dataset(config, d);
		ad.setSourceName(null);
		ads[0] = ad;
		
		return d;
	}

	private void assertIso(Model expected, Model obtained) {
		
//		System.err.println(">> expected:"); expected.write(System.err, RDFLanguages.strLangTurtle);
//		System.err.println(">> obtained:"); obtained.write(System.err, RDFLanguages.strLangTurtle);
//		System.err.println(">> isomorphic: " + (expected.isIsomorphicWith(obtained) ? "yes" : "no"));
		
		if (!expected.isIsomorphicWith(obtained)) {
			fail( "models were not isomorphic\nexpected:\n" + asString(expected) + "\nobtained:\n" + asString(obtained) + "\n.");
		} 
	}

	private String asString(Model m) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		m.write(bos, RDFLanguages.strLangTurtle);
		return bos.toString();
	}
}
