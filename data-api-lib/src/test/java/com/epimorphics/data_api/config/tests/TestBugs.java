/******************************************************************
 * File:        TestBugs.java
 * Created by:  Dave Reynolds
 * Created on:  25 Mar 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config.tests;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;

public class TestBugs {
    App testapp;
    
    @Before
    public void startup() throws IOException {
        testapp = new App("testapp", new File("src/test/data/bugs/test.conf"));
    }

    @Test
    public void testBasicConfig() {
        DSAPIManager man = testapp.getComponentAs("dsapi", DSAPIManager.class);
        API_Dataset dataset1 = man.getDataset("ppd");
        assertNotNull(dataset1);
    }
    
}
