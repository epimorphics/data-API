/******************************************************************
 * File:        TestConfig.java
 * Created by:  Dave Reynolds
 * Created on:  30 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config.tests;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;

public class TestConfig {
    App testapp;
    
    @Before
    public void startup() throws IOException {
        testapp = new App("testapp", new File("test/configTest/test2.conf"));
    }

    @Test
    public void testBasicConfig() {
        DSAPIManager man = testapp.getComponentAs("dsapi", DSAPIManager.class);
        Collection<API_Dataset> datasets = man.getDatasets();
        assertTrue(datasets.size() >= 2);
        
        // Inline case with an explicit base query 
        API_Dataset dataset = man.getDataset("wbclass-inline");
        assertEquals("wbclass-inline", dataset.getName()); 
        assertEquals("Waterbody classifications", dataset.getLabel()); 
        assertEquals("Waterbody classifications", dataset.getLabel("en")); 
        assertEquals("A data cube of waterbody classifications from EA catchment planning pilot", dataset.getDescription()); 
        assertEquals("?item qb:dataset classification:dataset", dataset.getBaseQuery());
        
        // Using DSD from the souce data
        dataset = man.getDataset("wbclass");
        assertEquals("?item  <http://purl.org/linked-data/cube#dataSet> <http://environment.data.gov.uk/data/waterbody/classification/dataset> .", dataset.getBaseQuery());
        
        dataset.getRoot().getModel().write(System.out, "Turtle");
    }
    
    @After
    public void cleanup() {
        if (testapp != null)
            testapp.shutdown();
    }

}
