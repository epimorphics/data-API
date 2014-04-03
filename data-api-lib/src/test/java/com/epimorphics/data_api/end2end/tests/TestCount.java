/******************************************************************
 * File:        TestCount.java
 * Created by:  Dave Reynolds
 * Created on:  2 Apr 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.end2end.tests;

import static com.epimorphics.data_api.config.tests.TestUtil.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestCount {
    App testapp;
    DSAPIManager man;
    
    @Before
    public void startup() throws IOException {
        testapp = new App("testapp", new File("src/test/data/countTest/test.conf"));
        man = testapp.getComponentAs("dsapi", DSAPIManager.class);
    }

    @Test
    public void testCounting() {
        API_Dataset dataset = man.getDataset("count-dataset");
        assertNotNull(dataset);
        
        List<Resource> results =  queryAsResultList(dataset, "{'eg:label' : { '@eq' : 'Thornbury'}}");
        assertEquals(16, results.size());
    
        int count = count(dataset, "{ '@count' : true, 'eg:label' : { '@eq' : 'Thornbury'} }");
        assertEquals(16, count);
        
        count = count(dataset, "{ '@count' : true, '@limit' : 10, 'eg:label' : { '@eq' : 'Thornbury'} }");
        assertEquals(10, count);
        
        count = count(dataset, "{ '@count' : true, '@limit' : 10, '@offset' : 10, 'eg:label' : { '@eq' : 'Thornbury'} }");
        assertEquals(6, count);
    }

    private int count(API_Dataset dataset, String query) {
        return count( queryAsJsonArray(dataset, query) );
    }
    
    private int count(JsonArray a) {
        JsonValue count = a.get(0).getAsObject().get("@count");
        return count.getAsNumber().value().intValue();
    }
}
