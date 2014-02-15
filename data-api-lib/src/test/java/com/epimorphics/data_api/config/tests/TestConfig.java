/******************************************************************
 * File:        TestConfig.java
 * Created by:  Dave Reynolds
 * Created on:  30 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
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
        checkAspects(dataset);
        
        JsonValue jv = asJson( man.asJson("en", "") );
        assertTrue(jv.isArray());
        JsonArray ja = jv.getAsArray();
        assertEquals(2, ja.size());
        JsonObject jds = ja.get(0).getAsObject();
        assertEquals("Waterbody classifications", jds.get("label").getAsString().value());
        assertEquals("A data cube of waterbody classifications from EA catchment planning pilot", jds.get("description").getAsString().value());
        assertTrue( jds.get("@id").getAsString().value().startsWith("http://www.epimorphics.com/test/dsapi/sprint2#wbclass") );
        assertTrue( jds.get("name").getAsString().value().startsWith("wbclass") );

        // Using DSD from the source data
        dataset = man.getDataset("wbclass");
        assertEquals("?item  <http://purl.org/linked-data/cube#dataSet> <http://environment.data.gov.uk/data/waterbody/classification/dataset>", dataset.getBaseQuery());
        checkAspects(dataset);
        
        jds = asJson( dataset.asJson("en", "") ).getAsObject();
        assertEquals("Waterbody classifications", jds.get("label").getAsString().value());
        ja = jds.get("aspects").getAsArray();
        assertEquals(5, ja.size());
    }
    
    private void checkAspects(API_Dataset dataset) {
        assertEquals(5, dataset.getAspects().size());
        
        Aspect aspect = aspectLabeled(dataset, "classification year");
        assertNotNull(aspect);
        assertEquals("The property classificationYear is a dimension property that relates an observation to a year for which a classification applies.", 
                aspect.getDescription("en"));
        assertFalse( aspect.getIsOptional() );
        assertEquals( XSD.gYear, aspect.getRangeType());
        
        aspect = aspectLabeled(dataset, "status or potential");
        assertTrue( aspect.getIsOptional() );

        JsonObject jo = asJson( aspect.asJson("en") ).getAsObject();
        assertEquals("wb-classification:statusOrPotential", jo.get("name").getAsString().value());
        assertEquals("http://environment.data.gov.uk/def/waterbody-classification/statusOrPotential", jo.get("@id").getAsString().value());
        assertEquals("status or potential", jo.get("label").getAsString().value());
        assertEquals("The property statusOrPotential is an attribute component property indicating whether the measure of the observation it is attached to is a status or potential measure.", jo.get("description").getAsString().value());
        assertEquals("http://environment.data.gov.uk/def/waterbody-classification/StatusOrPotential", jo.get("rangeType").getAsString().value());
        assertEquals(true, jo.get("isOptional").getAsBoolean().value());
        assertEquals(false, jo.get("isMultiValued").getAsBoolean().value());
    }
    
    private Aspect aspectLabeled(API_Dataset dataset, String label) {
        for (Aspect aspect : dataset.getAspects()) {
            if (label.equals( aspect.getLabel() )) {
                return aspect;
            }
        }
        return null;
    }
    
    private String asString(JSONWritable js) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JSFullWriter out = new JSFullWriter(bos);
        js.writeTo(out);
        out.finishOutput();
        return bos.toString();
    }
    
    private JsonValue asJson(JSONWritable js) {
        return JSON.parseAny( asString(js) );
    }
    
    @After
    public void cleanup() {
        if (testapp != null)
            testapp.shutdown();
    }

}
