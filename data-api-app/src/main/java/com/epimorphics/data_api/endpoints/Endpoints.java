/******************************************************************
 * File:        Endpoints.java
 * Created by:  Dave Reynolds
 * Created on:  1 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.endpoints;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.templates.VelocityRender;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.json.JSONWritable;

@Path("dataset")
public class Endpoints {
    protected DSAPIManager man;
    protected VelocityRender velocity;
    
    protected String lang = null;       // TODO set from request parameters
    
    protected @Context ServletContext context;
    protected @Context UriInfo uriInfo;
    protected @Context HttpServletRequest request;
    
    
    public DSAPIManager getManager() {
        if (man == null) {
            man = AppConfig.getApp().getA(DSAPIManager.class);
        }
        return man;
    }
    
    public String getBaseURI() {
        return uriInfo.getBaseUri() + "dataset/";
    }
    
    public VelocityRender getVelocity() {
        if (velocity == null) {
            velocity = AppConfig.getApp().getA(VelocityRender.class);
        }
        return velocity;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JSONWritable listDatasets() {
        try { return getManager().datasetsEndpoint(lang, getBaseURI()); }
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }

    }
    
    @GET
    @Produces(MediaType.TEXT_HTML)
    public StreamingOutput listDatasetsHtml() {
       try { return getVelocity().render("listDatasets.vm", uriInfo.getPath(), context, uriInfo.getQueryParameters()); }        
       catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }
    }
    
    @GET
    @Path("/{dataset}")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONWritable getDataset(@PathParam("dataset") String dsid) {
        try { return getManager().datasetEndpoint(lang, dsid, getBaseURI()); }
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }
    }
    
    @GET
    @Path("/{dataset}/structure")
    @Produces(MediaType.APPLICATION_JSON)
    public JSONWritable getDatasetStructure(@PathParam("dataset") String dsid) {
        try { return getManager().datasetStructureEndpoint(lang, dsid, getBaseURI()); }        
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }

    }
    
    @GET
    @Path("/{dataset}/structure")
    @Produces(MediaType.TEXT_HTML)
    public StreamingOutput getDatasetStructureHtml(@PathParam("dataset") String dsid) {
        try { return getVelocity().render("datasetStructure.vm", uriInfo.getPath(), context, uriInfo.getQueryParameters(), "dataset", dsid); }       
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }

    }
    
    @POST
    @Path("/{dataset}/data")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public JSONWritable getDataJSON(@PathParam("dataset") String dsid, JsonObject query) {
        try { return getManager().datasetDataEndpoint(lang, dsid, query); }
        catch (WebApiException w) { throw w; }
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }
    }
    
    @GET
    @Path("/{dataset}/data")
    @Produces(MediaType.TEXT_HTML)
    public StreamingOutput getDataHtml(@PathParam("dataset") String dsid) {
       try { return getVelocity().render("getData.vm", uriInfo.getPath(), context, uriInfo.getQueryParameters(), "dataset", dsid);}      
       catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }
    }
    
    @POST
    @Path("/{dataset}/explain")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response explainQuery(@PathParam("dataset") String dsid, JsonObject query) {
        try { return getManager().datasetExplainEndpoint(dsid, query); }    
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }
    }
    
    @GET
    @Path("/{dataset}/describe")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDescriptionByGET(@PathParam("dataset") String dsid, @QueryParam("uri") List<String> uris) {
        try { return getManager().datasetDescribeEndpoint(dsid, uris); }       
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }
    }
    
    @POST
    @Path("/{dataset}/labels")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getLabelsByPOST(@PathParam("dataset") String dsid, JsonObject query) {
        try { return getManager().datasetGetLabels(dsid, query); }
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }
    }
    
    @POST
    @Path("/{dataset}/describe")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getDescriptionByPOST(@PathParam("dataset") String dsid, JsonObject query) {
        try { return getManager().datasetDescribeEndpoint(dsid, query); }
        catch(Throwable t) { throw new WebApiException(Status.INTERNAL_SERVER_ERROR, t.toString()); }
    }
    
}
