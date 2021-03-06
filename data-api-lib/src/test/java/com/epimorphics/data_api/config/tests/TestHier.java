/******************************************************************
 * File:        TestHeir.java
 * Created by:  Dave Reynolds
 * Created on:  14 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config.tests;

import static com.epimorphics.data_api.config.tests.TestConfig.asJson;
import static com.epimorphics.data_api.config.tests.TestConfig.aspectLabeled;
import static com.epimorphics.data_api.config.tests.TestUtil.queryAsResultList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.jena.atlas.json.JsonObject;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.vocabs.Cube;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class TestHier {
    App testapp;
    DSAPIManager man;
    
    @Before
    public void startup() throws IOException {
        testapp = new App("testapp", new File("src/test/data/configTest/testHier.conf"));
        man = testapp.getComponentAs("dsapi", DSAPIManager.class);
    }

    @Test
    // Could split this into separate tests but this way keeps the startup log noise down
    public void testHierarchies() {
        // Concept scheme tests
        API_Dataset dataset = man.getDataset("assessment-method");
        assertNotNull(dataset);
        assertEquals("?item skos:inScheme <http://ukgovld-registry.dnsalias.net/def/education/isb/assessment-method-type> .", dataset.getBaseQuery().trim());
        // Test basic membership
        List<Resource> results =  queryAsResultList(dataset, "{}");
        assertEquals(32, results.size());
        
        // Test root listing
        results = queryAsResultList(dataset, "{ '@childof' : null }");
        assertEquals(17, results.size());
        assertTrue(results.contains( ResourceFactory.createResource("http://ukgovld-registry.dnsalias.net/def/education/isb/assessment-method-type/performance") ));
        
        // Test child listing
        results = queryAsResultList(dataset, "{ '@childof' : 'amt:performance' }");
        assertEquals(15, results.size());
        
        // Collection tests
        dataset = man.getDataset("categories");
        assertNotNull(dataset);
        assertEquals("<http://ukgovld-registry.dnsalias.net/def/dataset-categories> skos:member ?item .", dataset.getBaseQuery().trim());
        results =  queryAsResultList(dataset, "{}");
        assertEquals(12, results.size());
        assertTrue(results.contains( ResourceFactory.createResource("http://ukgovld-registry.dnsalias.net/def/dataset-categories/defence") ));
        results = queryAsResultList(dataset, "{ '@childof' : null }");
        assertEquals(12, results.size());
        results = queryAsResultList(dataset, "{ '@childof' : 'http://ukgovld-registry.dnsalias.net/def/dataset-categories/defence' }");
        assertTrue( results.isEmpty() );
        
        // Hierarchy Code list tests
        dataset = man.getDataset("areas");
        assertNotNull(dataset);
        results = queryAsResultList(dataset, "{ '@childof' : null }");
        assertTrue(results.contains( ResourceFactory.createResource("http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/1") ));
        assertEquals(6, results.size());
        results = queryAsResultList(dataset, "{ '@childof' : 'http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/1' }");
        assertEquals(3, results.size());
        results =  queryAsResultList(dataset, "{}");
        assertEquals(22, results.size());
        assertTrue(results.contains( ResourceFactory.createResource("http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/10-36") ));
        
        // Aspect codelist tests
        dataset = man.getDataset("ea-data");
        assertNotNull(dataset);
        assertEquals(3, dataset.getAspects().size());
        Aspect a = aspectLabeled(dataset, "area");
        assertEquals("ea-areas-hcl", a.getRangeDataset());
        JsonObject jo = asJson( a.asJson("en") ).getAsObject();
        assertEquals("ea-areas-hcl", jo.get("rangeDataset").getAsString().value());

        a = aspectLabeled(dataset, "year");
        assertEquals("dummy-year-dataset", a.getRangeDataset());
        
        dataset = man.getDataset("ea-areas-hcl");
        assertNotNull(dataset);
                
        results = queryAsResultList(dataset, "{ '@childof' : null }");
        assertEquals(6, results.size());
        assertTrue(results.contains( ResourceFactory.createResource("http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/1") ));
        
        // JSON serialization of dataset
        jo = asJson( dataset.asJsonShort("en", "http://localhost") ).getAsObject();
        assertNotNull(jo);
        jo = jo.get("hierarchy").getAsObject();
        assertNotNull(jo);
        assertEquals("http://www.epimorphics.com/test/dsapi/sprint3#ea-areas-hcl", jo.get("@id").getAsString().value());
        assertEquals(Cube.HierarchicalCodeList.getURI(), jo.get("type").getAsString().value());
        
        // Define via qb:codeList on the DSD
        dataset = man.getDataset("ea-data2");
        assertNotNull(dataset);
        assertEquals(3, dataset.getAspects().size());
        a = aspectLabeled(dataset, "area");
        assertEquals("ea-areas-hcl2", a.getRangeDataset());

        dataset = man.getDataset("ea-areas-hcl2");
        assertNotNull(dataset);
        results = queryAsResultList(dataset, "{ '@childof' : null }");
        assertEquals(6, results.size());
        
        // Below query applied to hierarchical code lists
        dataset = man.getDataset("ea-data");
        results = queryAsResultList(dataset, "{ 'data:area' : {'@below' : {'@id' : 'http://environment.data.gov.uk/registry/def/ea-organization/ea_areas/10'} } }");
        assertEquals(5, results.size());
        assertTrue(results.contains( ResourceFactory.createResource("http://www.epimorphics.com/test/dsapi/sprint3-data#obs1")));
        assertTrue(results.contains( ResourceFactory.createResource("http://www.epimorphics.com/test/dsapi/sprint3-data#obs3")));
    }

}
