/******************************************************************
 * File:        TestSearch.java
 * Created by:  Dave Reynolds
 * Created on:  20 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.search.tests;

import static com.epimorphics.data_api.config.tests.TestUtil.queryAsResultList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestSearch {
    App testapp;
    
    @Before
    public void startup() throws IOException {
        testapp = new App("testapp", new File("src/test/data/searchTest/test.conf"));
    }

    @Test
    public void testBasicConfig() {
        DSAPIManager man = testapp.getComponentAs("dsapi", DSAPIManager.class);
        API_Dataset dataset = man.getDataset("search-dataset");
        assertNotNull(dataset);
        
        List<Resource> results =  queryAsResultList(dataset, "{}");
        assertEquals(10, results.size());

        // Search on aspect whose values are labeled resources
        results =  queryAsResultList(dataset, "{'eg:resource' : {'@search' : 'Thornbury'}}");	
        assertEquals(4, results.size());
        
        // Global search for dataset items which are labeled by some indexed property
        results =  queryAsResultList(dataset, "{'@search' : 'Thornbury'}");
        assertEquals(4, results.size());
        
        // Search on aspect whose values are strings, where aspect property has been included in the text index
//        results =  query(dataset, "{'eg:label' : {'@search' : 'Thornbury'}}");
//        assertEquals(4, results.size());
    }
 
}
