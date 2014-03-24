/******************************************************************
 * File:        TestMultiSrc.java
 * Created by:  Dave Reynolds
 * Created on:  24 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config.tests;

import static com.epimorphics.data_api.config.tests.TestUtil.query;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestMultiSrc {
    App testapp;
    
    @Before
    public void startup() throws IOException {
        testapp = new App("testapp", new File("src/test/data/multiSrcTest/test.conf"));
    }

    @Test
    public void testBasicConfig() {
        DSAPIManager man = testapp.getComponentAs("dsapi", DSAPIManager.class);
        API_Dataset dataset1 = man.getDataset("dataset1");
        assertNotNull(dataset1);
        
        List<Resource> results =  query(dataset1, "{'eg:label' : {'@eq' : 'Somerset'}}");
        assertEquals(2, results.size());
        assertTrue( results.get(0).getURI().startsWith("http://www.epimorphics.com/test/dsapi/sprint3/multiSrc/data1/"));
        
        API_Dataset dataset2 = man.getDataset("dataset2");
        assertNotNull(dataset2);
        results =  query(dataset2, "{'eg:label' : {'@eq' : 'Somerset'}}");
        assertEquals(2, results.size());
        assertTrue( results.get(0).getURI().startsWith("http://www.epimorphics.com/test/dsapi/sprint3/multiSrc/data2/"));
    }
    
}
