/******************************************************************
 * File:        TestHeir.java
 * Created by:  Dave Reynolds
 * Created on:  14 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.rdfutil.QueryUtil;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class TestHier {
    App testapp;
    DSAPIManager man;
    
    @Before
    public void startup() throws IOException {
        testapp = new App("testapp", new File("src/test/data/configTest/testHier.conf"));
        man = testapp.getComponentAs("dsapi", DSAPIManager.class);
    }

    @Test
    public void testHierarchies() {
        // Concept scheme tests
        API_Dataset dataset = man.getDataset("assessment-method");
        assertNotNull(dataset);
        assertEquals("?item skos:inScheme <http://ukgovld-registry.dnsalias.net/def/education/isb/assessment-method-type> .", dataset.getBaseQuery().trim());
        // Test basic membership
        List<Resource> results =  query(dataset, "{}");
        assertEquals(32, results.size());
        
        // Test root listing
        results = query(dataset, "{ '@childof' : null }");
        assertEquals(17, results.size());
        assertTrue(results.contains( ResourceFactory.createResource("http://ukgovld-registry.dnsalias.net/def/education/isb/assessment-method-type/performance") ));
        
        // Test child listing
        results = query(dataset, "{ '@childof' : 'amt:performance' }");
        assertEquals(15, results.size());
        
        // Collection tests
        dataset = man.getDataset("categories");
        assertNotNull(dataset);
        assertEquals("<http://ukgovld-registry.dnsalias.net/def/dataset-categories> skos:member ?item .", dataset.getBaseQuery().trim());
        results =  query(dataset, "{}");
        assertEquals(12, results.size());
        assertTrue(results.contains( ResourceFactory.createResource("http://ukgovld-registry.dnsalias.net/def/dataset-categories/defence") ));
        results = query(dataset, "{ '@childof' : null }");
        assertEquals(12, results.size());
        results = query(dataset, "{ '@childof' : 'http://ukgovld-registry.dnsalias.net/def/dataset-categories/defence' }");
        assertTrue( results.isEmpty() );
        
        // Hierarchy Code list tests
        dataset = man.getDataset("areas");
        assertNotNull(dataset);
        results = query(dataset, "{ '@childof' : null }");
        assertTrue(results.contains( ResourceFactory.createResource("http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/1") ));
        assertEquals(6, results.size());
        results = query(dataset, "{ '@childof' : 'http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/1' }");
        assertEquals(3, results.size());
        results =  query(dataset, "{}");
        assertEquals(22, results.size());
        assertTrue(results.contains( ResourceFactory.createResource("http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/10-36") ));
        
        // Aspect codelist tests
        dataset = man.getDataset("ea-data");
        assertNotNull(dataset);
        assertEquals(3, dataset.getAspects().size());
        for (Aspect a : dataset.getAspects()) {
            if (a.getName().getCURIE().equals("data:area")) {
                assertEquals("ea-areas-hcl", a.getRangeDataset());
            }
        }
        dataset = man.getDataset("ea-areas-hcl");
        assertNotNull(dataset);
        results = query(dataset, "{ '@childof' : null }");
        assertTrue(results.contains( ResourceFactory.createResource("http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/1") ));
        assertEquals(6, results.size());
        
    }

    private List<Resource> query(API_Dataset dataset, String json) {
        JsonObject query = JSON.parse(json);
        Problems p = new Problems();
        DataQuery q = DataQueryParser.Do(p, dataset, query);
        String sq = null;
        if (p.isOK()) {
            sq = q.toSparql(p, dataset);
//            System.out.println(sq);
        }
        if (p.isOK()) {
            ResultSet results = man.getSource().select(sq);
            return QueryUtil.resultsFor(results, "item");
        } else {
            throw new EpiException("Failed to parse query: " + p.getProblemStrings());
        }
        
    }
}
