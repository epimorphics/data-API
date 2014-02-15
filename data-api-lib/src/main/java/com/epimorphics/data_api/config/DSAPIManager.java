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
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.conversions.ResultsToJson;
import com.epimorphics.data_api.conversions.ResultsToJson.JSONConsumer;
import com.epimorphics.data_api.conversions.ResultsToJson.Row;
import com.epimorphics.data_api.conversions.ResultsToJson.RowConsumer;
import com.epimorphics.data_api.conversions.Value;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.endpoints.support.StreamFromResults;
import com.epimorphics.data_api.endpoints.support.StreamFromResults.Bool;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.libs.JSONLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.util.EpiException;
import com.github.jsonldjava.core.JSONLDConsts;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
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
    protected boolean checkingSPARQL;
    
    public boolean getCheckingSPARQL() {
    	return checkingSPARQL;
    }
    public void setCheckingSPARQL(boolean checkingSPARQL) {
    	this.checkingSPARQL = checkingSPARQL;
    }

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
        <pre>POST query to base/dataset/{dataset}/describe</pre>
     
     	<p>
     	Returns a JSON-LD serialisation of the description of the resource
     	described by the posted query, a JSON string of the URI of the
     	resource.
     	</p>
     	
     	<p>
     		TODO: alternative way of streaming from model, attaching into Jersey.
     		TODO: decide what's to be done about the dataset.
     		TODO: decide whethe the resource should be passed as a query
     			parameter, or as a {@id: URI} object for consistency.
     	</p>  
     	
     	<p>
     	   	I provoked this from the command line to get some results:
     	   	
     	   	<pre>
     	   	wget --header='Content-Type: application/json' --header='Accept-Type: application/json' --post-data='{"@id": "http://boardgamegeek.com/boardgamedesigner/34699/matthias-cramer"}' http://localhost:8080/dsapi/dataset/games/describe
			</pre>
     	   	
     	</p>
    */
    public Response datasetDescribeEndpoint(String dataset, JsonObject query) {
    	String resource = query.getAsObject().get("@id").getAsString().value();    	
    	final Model m = ModelFactory.createModelForGraph(source.describe("DESCRIBE <" + resource + ">"));
    	StreamingOutput description = new StreamingOutput() {

			@Override public void write(OutputStream output) throws IOException, WebApplicationException {
				m.write(output, "JSON-LD");
				
			}};
    	
    	return Response.ok(description).build();
    }
    
    public Response datasetDescribeEndpoint(String dataset, List<String> uris) {   	
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append("DESCRIBE");
    	for (String u: uris) sb.append("\n <").append(u).append(">");
    	String query = sb.append("\n").toString();
    	
    	final Model m = ModelFactory.createModelForGraph(source.describe(query));
    	StreamingOutput description = new StreamingOutput() {

			@Override public void write(OutputStream output) throws IOException, WebApplicationException {
				m.write(output, "JSON-LD");
				
			}};
    	
    	return Response.ok(description).build();
    }
    
    /**
     * <pre>base/dataset/{dataset}/data</pre>
     * 
     * The data query endpoint
     */
    public JSONWritable datasetDataEndpoint(String lang, String dataset, JsonObject query) {
        Problems p = new Problems();
        RowWriter so = null;        
        final API_Dataset api = getAPI(dataset);
        try {
            DataQuery q = DataQueryParser.Do(p, api.getPrefixes(), query);
            log.info("Request: " + query.toString());
            String sq = null;
            if (p.isOK()) {
                sq = q.toSparql(p, api);
                if (checkingSPARQL) checkLegalSPARQL(p, sq);
            }

            if (p.isOK()) {
                log.info("Issuing query: " + sq);
                so = new RowWriter(api.getAspects(), source.select(sq));
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
            return so;

        } else {
            String problemStrings = p.getProblemStrings();
            throw new WebApiException(Status.BAD_REQUEST, "FAILED:\n" + BunchLib.join(problemStrings));
        }
    }
    
    public final class RowWriter implements JSONWritable {
    	
    	private final Set<Aspect> aspects;
    	private final ResultSet rs;

    	public RowWriter(Set<Aspect> aspects, ResultSet rs) {
    		this.aspects = aspects;
    		this.rs = rs;
    	}

    	@Override public void writeTo(final JSFullWriter jw) {
    		final Bool comma = new Bool();
    		jw.startArray();
    		
    		RowConsumer stream = new RowConsumer() {
    			
    			@Override public void consume(Row jv) {
    				if (comma.value) jw.arraySep(); 
    				jv.writeTo(jw);
    				comma.value = true;
    			}
    		};
    		ResultsToJson.convert(aspects, stream, rs);
    		jw.finishArray();
    	}
    }
    
    static final StreamingOutput StreamNothing = new StreamingOutput() {
		
		@Override public void write(OutputStream output) throws IOException, WebApplicationException {
			// No bytes at all
		}
	};
    
    /**
     * <pre>base/dataset/{dataset}/explain</pre>
     * 
     * Return a summary of the translation of a query as a json object (not streaming)
     */
    public Response datasetExplainEndpoint(String dataset, JsonObject query) {
        API_Dataset api = getAPI(dataset);

        Problems p = new Problems();
        
        JsonObject comments = new JsonObject();

        comments.put("datasetName", api.getName());
        comments.put("request", query.toString());

        JsonArray aspects = new JsonArray();
        for (Aspect a : api.getAspects()) {
            Resource rt = a.getRangeType();
            boolean optional = a.getIsOptional(), multiple = a.getIsMultiValued();
            aspects.add("" + a + (rt == null ? "" : " [range: " + rt + "]")
                + (optional ? ", optional" : "")
                + (multiple ? ", multivalued" : ""));
        }
        comments.put("aspects", aspects);

        try {
            DataQuery q = null;
            String sq = null;

            if (p.isOK())
                q = DataQueryParser.Do(p, api.getPrefixes(), query);

            if (p.isOK()) {
                sq = q.toSparql(p, api);
                if (checkingSPARQL) checkLegalSPARQL(p, sq);
            }
            
            if (p.isOK()) comments.put("sparql", QueryFactory.create(sq).toString());

            if (p.isOK()) {
                long start = System.currentTimeMillis();
                ResultSet rs = source.select(sq);
                RowConsumer discard = new RowConsumer() {
					
					@Override public void consume(Row jv) {						
					}
				};
                ResultsToJson.convert(api.getAspects(), discard, rs);
                long finish = System.currentTimeMillis();
                comments.put("time", finish-start);
            }

        } catch (Exception e) {
            System.err.println("BROKEN: " + e);
            e.printStackTrace(System.err);
            p.add("BROKEN: " + e);
        }
        
        comments.put("status", p.isOK());

        return Response.ok(comments.toString()).build();
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
