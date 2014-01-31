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
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.hp.hpl.jena.vocabulary.XSD;

public class TestConfig {
    App testapp;
    
    @Before
    public void startup() throws IOException {
        testapp = new App("testapp", new File("src/test/data/configTest/test2.conf"));
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

        assertEquals(5, dataset.getAspects().size());
        Aspect aspect = aspectLabeled(dataset, "classification year");
        assertNotNull(aspect);
        assertFalse( aspect.getIsOptional() );
        assertEquals( XSD.gYear, aspect.getRangeType());
        
        aspect = aspectLabeled(dataset, "status or potential");
        assertTrue( aspect.getIsOptional() );

        // Using DSD from the source data
        dataset = man.getDataset("wbclass");
        assertEquals("?item  <http://purl.org/linked-data/cube#dataSet> <http://environment.data.gov.uk/data/waterbody/classification/dataset> .", dataset.getBaseQuery());
        assertEquals(5, dataset.getAspects().size());
        
        aspect = aspectLabeled(dataset, "classification year");
        assertNotNull(aspect);
        assertEquals("The property classificationYear is a dimension property that relates an observation to a year for which a classification applies.", 
                aspect.getDescription("en"));
        assertFalse( aspect.getIsOptional() );
        assertEquals( XSD.gYear, aspect.getRangeType());
        
        aspect = aspectLabeled(dataset, "status or potential");
        assertTrue( aspect.getIsOptional() );
        
    }
    
    private Aspect aspectLabeled(API_Dataset dataset, String label) {
        for (Aspect aspect : dataset.getAspects()) {
            if (label.equals( aspect.getLabel() )) {
                return aspect;
            }
        }
        return null;
    }
    
    @After
    public void cleanup() {
        if (testapp != null)
            testapp.shutdown();
    }

}
