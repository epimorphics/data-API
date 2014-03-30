/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.translation.tests;

import java.io.File;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.test_support.Asserts;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.vocabs.Dsapi;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestJSONtoSPARQL {
	
	static File dataDir = new File("src/test/data/jsonToSparql");
	
	@Test public void testFromDataDir() {
		testMe( "gamesAPI-search_or_search");
	}
	
	void testMe(String name) {
		File jsonFile = new File(dataDir, name + ".json");
		File sparqlFile = new File(dataDir, name + ".rq" );
		File configFile = new File(dataDir, shorten(name) + ".ttl" );
		
		Model configModel= FileManager.get().loadModel(configFile.getPath());
		
		Resource config = configModel.listSubjectsWithProperty(RDF.type, Dsapi.Dataset)
			.toList()
			.get(0)
			;
		
//		System.err.println( ">> CONFIG: " + config );
		
		DSAPIManager manager = new DSAPIManager();
		API_Dataset ds = new API_Dataset(config, manager);
		Aspects a = andTheAspectsToo(ds, config);
		
		PrefixMapping pm = ds.getPrefixes();
		
//		System.err.println( ">> PREFIXES: " + pm.getNsPrefixMap() );
//		System.err.println( ">> ASPECTS: " + ds.getAspects());
		
		String json = FileManager.get().readWholeFileAsUTF8( jsonFile.getPath());
		String sparql = FileManager.get().readWholeFileAsUTF8(sparqlFile.getPath());
		
		JsonObject jo = JSON.parse(json);
		Problems p = new Problems();
		DataQuery dq = DataQueryParser.Do(p, ds, jo);

//		Asserts.assertNoProblems("JSON query did not parse", p);
		
		String generated = dq.toSparql(p, a, ds.getBaseQuery(), pm);
		
		String expected = sparql;
		Asserts.assertSameSelect(expected, generated);
	}

	// TODO integrate properly with monitor code
	private Aspects andTheAspectsToo(API_Dataset ds, Resource config) {
		Aspects aspects = new Aspects();
		for (RDFNode x: config.listProperties(Dsapi.aspect).mapWith(Statement.Util.getObject).toList()) {
			Resource rx = (Resource) x;
			Aspect a = new Aspect(rx);
			a.setIsOptional( RDFUtil.getBooleanValue(rx, Dsapi.optional, false));
			a.setIsMultiValued( RDFUtil.getBooleanValue(rx, Dsapi.multivalued, false));
			a.setPropertyPath( RDFUtil.getStringValue(rx, Dsapi.propertyPath));
			ds.add(a);
			aspects.include(a);
		}
		return aspects;
	}

	private String shorten(String name) {
		int dash = name.lastIndexOf('-');
		if (dash < 0) throw new BrokenException("no prefix-splitting '-' in '" + name + "'");
		return name.substring(0, dash);
	}
	
}
