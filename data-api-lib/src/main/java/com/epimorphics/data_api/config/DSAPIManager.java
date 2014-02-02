/******************************************************************
 * File:        DSAPIManager.java
 * Created by:  Dave Reynolds
 * Created on:  28 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.data_api.conversions.ResultsToJson;
import com.epimorphics.data_api.conversions.ResultsToJson.JSONConsumer;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.libs.JSONLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.sun.jersey.api.NotFoundException;

/**
 * An appbase component which manages a set of DSAPI dataset end points.
 * <p>
 * To configure an instance of this need at least a (sparql) source, apiBase
 * and a DatasetMonitor which manages the scanning of a directory for configuration instances.
 * The apiBase defines the root of all the apis provided by this manager instance. Typically
 * this will be a server relative context path ("/dsapi/foo").
 * </p><p>
 * The DatasetMonitor is separately configured to allow easy setting of parameters like productionMode.
 * </p><p>
 * The json representation is an array of data set descriptions.
 * </p>
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DSAPIManager extends ComponentBase {
    static Logger log = LoggerFactory.getLogger(DSAPIManager.class);

    protected SparqlSource source;
    protected DatasetMonitor monitoredDatasets;
    protected String apiBase;

    public SparqlSource getSource() {
        return source;
    }

    public void setSource(SparqlSource source) {
        this.source = source;
    }

    public String getApiBase() {
        return apiBase;
    }

    public void setApiBase(String apiBase) {
        this.apiBase = apiBase;
    }

    public void setMonitoredDatasets(DatasetMonitor monitoredDatasets) {
        this.monitoredDatasets = monitoredDatasets;
        monitoredDatasets.setManager(this);
    }

    public Collection<API_Dataset> getDatasets() {
        return monitoredDatasets.getEntries();
    }
    
    public API_Dataset getDataset(String name) {
        return monitoredDatasets.get(name);
    }

    public JSONWritable asJson(String lang) {
        return new Writer(lang);
    }

    public void writeJson(JSFullWriter out, String lang) {
        out.startArray();
        for (Iterator<API_Dataset> i = getDatasets().iterator(); i.hasNext();) {
            i.next().writeShortTo(out, lang);
            if (i.hasNext()) {
                out.arraySep();
            }
        }
        out.finishArray();
    }
    
    class Writer implements JSONWritable {
        String lang;
        public Writer(String lang) {  this.lang = lang;  }
        
        @Override
        public void writeTo(JSFullWriter out) {
            writeJson(out, lang);
        }
    }
    
    // Endpoint implementations - should be called from app-specific jersey bindings
    
    /**
     * <pre>base/dataset</pre>
     * 
     * Return a JSON serialization of the list of available datasets
     *  
     * @param lang two-char language code giving preferred language for labels etc, null is allowed as a default
     */
    public JSONWritable datasetsEndpoint(String lang) {
        return asJson(lang);
    }
    
    /**
     * <pre>base/dataset/{dataset}</pre>
     * 
     * Return a JSON serialization of the short form for a specific dataset, includes references
     * to the endpoints for structure and data
     *  
     * @param lang two-char language code giving preferred language for labels etc, null is allowed as a default
     */
    public JSONWritable datasetEndpoint(String lang, String dataset) {
        return  getAPI(dataset).asJsonShort(lang); 
    }

    private API_Dataset getAPI(String dataset) {
        API_Dataset api = getDataset(dataset);
        if (api == null) {
            throw new NotFoundException("Dataset " + dataset + " not registered");
        }
        return api;
    }
    
    /**
     * <pre>base/dataset/{dataset}/structure</pre>
     * 
     * Return a JSON serialization of the full structure of a data set
     *  
     * @param lang two-char language code giving preferred language for labels etc, null is allowed as a default
     */
    public JSONWritable datasetStructureEndpoint(String lang, String dataset) {
        return  getAPI(dataset).asJson(lang); 
    }
    
    /**
     * <pre>base/dataset/{dataset}/data</pre>
     * 
     * The data query endpoint
     */
    public Response datasetDataEndpoint(String lang, String dataset, JsonObject query) {
        Problems p = new Problems();
        final JsonArray result = new JsonArray();
        
        API_Dataset api = getAPI(dataset);
        try {
            DataQuery q = DataQueryParser.Do(p, api.getPrefixes(), query);
            log.info("Request: " + query.toString());
            String sq = null;
            if (p.isOK()) {
                sq = q.toSparql(p, api);
            }

            // TODO is this needed for every call?
            checkLegalSPARQL(p, sq);

            if (p.isOK()) {
                log.info("Issuing query: " + sq);
                ResultSet rs = source.select(sq);
                JSONConsumer toResult = JSONLib.consumeToArray(result);
                ResultsToJson.convert(api.getAspects(), toResult, rs);
            }

        } catch (Exception e) {
            System.err.println("BROKEN: " + e);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(os);
            e.printStackTrace(ps);
            ps.flush();
            try {
                p.add("BROKEN: " + e + "\n" + os.toString("UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                throw new EpiException("UTF-8 not supported?!");
            }
        }
                
        if (p.isOK()) {
            return Response.ok(result.toString()).build();

        } else {
            String problemStrings = p.getProblemStrings();
            throw new WebApiException(Status.BAD_REQUEST, "FAILED:\n" + BunchLib.join(problemStrings));
        }
    }

    // TODO query explain call - but maybe on the fly rather than via historical store

    private void checkLegalSPARQL(Problems p, String sq) {
        if (p.isOK()) {
            try {
                QueryFactory.create(sq);
            } catch (Exception e) {
                p.add("Bad generated SPARQL:\n" + sq + "\n" + e.getMessage());
            }
        }
    }

}
