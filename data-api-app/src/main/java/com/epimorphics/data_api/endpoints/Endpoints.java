/******************************************************************
 * File:        Endpoints.java
 * Created by:  Dave Reynolds
 * Created on:  1 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.endpoints;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.json.JSONWritable;

@Path("dataset")
public class Endpoints {
    protected DSAPIManager man;
    protected String lang = null;       // TODO set from request parameters
    
    public DSAPIManager getManager() {
        if (man == null) {
            man = AppConfig.getApp().getA(DSAPIManager.class);
        }
        return man;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JSONWritable listDatasets() {
        return getManager().datasetsEndpoint(lang);
    }
    
    @GET
    @Path("/{dataset}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONWritable getDataset(@PathParam("dataset") String dsid) {
        return getManager().datasetEndpoint(lang, dsid);
    }
    
    @GET
    @Path("/{dataset}/structure")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONWritable getDatasetStructure(@PathParam("dataset") String dsid) {
        return getManager().datasetStructureEndpoint(lang, dsid);
    }
    
    @POST
    @Path("/{dataset}/data")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getData(@PathParam("dataset") String dsid, JsonObject query) {
        return getManager().datasetDataEndpoint(lang, dsid, query);
    }

}
