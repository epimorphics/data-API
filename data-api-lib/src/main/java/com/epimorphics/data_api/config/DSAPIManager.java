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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.data.ClosableResultSet;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.data.impl.RemoteSparqlSource;
import com.epimorphics.appbase.webapi.WebApiException;
import com.epimorphics.data_api.Switches;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.SKOS;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

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
    
    static { log.info(DataQuery.DSAPI_Info); }

    static final String JSON_LD = RDFLanguages.strLangJSONLD;    
    
    protected SparqlSource defaultSource;
    protected Map<String, SparqlSource> sources = new HashMap<String, SparqlSource>();
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
        return defaultSource;
    }

    public SparqlSource getSource(String sourceName) {
    	
    	System.err.println(">> --------------------------------- SOURCES: " + sources.keySet());
    	
        if (sourceName == null) {
            return defaultSource;
        }
        return sources.get(sourceName);
    }

    public void setSource(SparqlSource source) {
        this.defaultSource = source;
    }
    
    public void setSources(List<SparqlSource> srcs) {
        if (srcs.size() > 0) {
            defaultSource = srcs.get(0);
            for (SparqlSource s : srcs) {
                sources.put(s.getName(), s);
            }
        }
    }

    public static Map<String, String> failures() {
    	return AppConfig.getFailures();
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

    public JSONWritable asJson(String lang, String uribase) {
        return new Writer(lang, uribase);
    }

    public void writeJson(JSFullWriter out, String lang, String uribase) {
        out.startArray();
        for (Iterator<API_Dataset> i = getDatasets().iterator(); i.hasNext();) {
            i.next().writeShortTo(out, lang, uribase);
            if (i.hasNext()) {
                out.arraySep();
            }
        }
        out.finishArray();
    }
    
    class Writer implements JSONWritable {
        String lang;
        String uribase;
        public Writer(String lang, String uribase) {  this.lang = lang;  this.uribase = uribase; }
        
        @Override
        public void writeTo(JSFullWriter out) {
            try { writeJson(out, lang, uribase); }
            catch (Exception e) {
            	log.error("failed to srite JSON: " + e);
            }
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
    public JSONWritable datasetsEndpoint(String lang, String uribase) {
        return asJson(lang, uribase);
    }
    
    /**
     * <pre>base/dataset/{dataset}</pre>
     * 
     * Return a JSON serialization of the short form for a specific dataset, includes references
     * to the endpoints for structure and data
     *  
     * @param lang two-char language code giving preferred language for labels etc, null is allowed as a default
     */
    public JSONWritable datasetEndpoint(String lang, String dataset, String uribase) {
        return  getAPI(dataset).asJsonShort(lang, uribase); 
    }

    public API_Dataset getAPI(String dataset) {
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
    public JSONWritable datasetStructureEndpoint(String lang, String dataset, String uribase) {
        return  getAPI(dataset).asJson(lang, uribase); 
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
    	SparqlSource source = getAPI(dataset).getSource();
    	final Model m = ModelFactory.createModelForGraph(source.describe("DESCRIBE <" + resource + ">"));
    	StreamingOutput description = new StreamingOutput() {

			@Override public void write(OutputStream output) throws IOException, WebApplicationException {
				m.write(output, JSON_LD);
				
			}};
    	
    	return Response.ok(description).build();
    }
    
    public Response datasetDescribeEndpoint(String dataset, List<String> uris) {   	
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append("DESCRIBE");
    	for (String u: uris) sb.append("\n <").append(u).append(">");
    	String query = sb.append("\n").toString();
    //
        SparqlSource source = getAPI(dataset).getSource();        
        final Model m = ModelFactory.createModelForGraph(source.describe(query));
    	StreamingOutput description = new StreamingOutput() {

			@Override public void write(OutputStream output) throws IOException, WebApplicationException {
				m.write(output, JSON_LD);
				
			}};
    	
    	return Response.ok(description).build();
    }
    
    public List<String> getItems(JsonValue query) {
    	List<String> items = new ArrayList<String>();
    	if (query.isArray())
    		for (JsonValue x: query.getAsArray())
    			items.add(asItem(x));
    	return items;
    }
    
	private String asItem(JsonValue x) {
		if (x.isString()) return x.getAsString().value();
		if (x.isObject()) return x.getAsObject().get("@id").getAsString().value();
		// TODO fix this problem report to cause a 400.
		throw new RuntimeException("Expected URI string or @id object: " + x);
	}
	
	// If no labels properies are requested, use these.
	static final List<String> defaultLabelProperties =
		Arrays.asList(new String[] {
			RDFS.label.getURI()
			, SKOS.prefLabel.getURI()
		} );
    
    private List<String> getLabelProperties(JsonValue jv) {
    	List<String> items = new ArrayList<String>();
    	if (jv == null) {
    		items.addAll(defaultLabelProperties);
    	} else if (jv.isArray()) {
    		for (JsonValue p: jv.getAsArray())
    			items.add(asItem(p));
    	}
		return items;
	}
	
	public Response datasetGetLabels(String dsid, JsonObject query) {   		
	// TODO diagnostics when @things missing.
		List<String> items = getItems(query.get("@items"));
		List<String> labelProperties = getLabelProperties(query.get("@properties"));
	//
		JsonValue jSource = query.get("@source");
		String sourceName = jSource.isString() ? jSource.getAsString().value() : null;
		SparqlSource source = sourceName == null ? getSource() : getSource( sourceName );
	//
		int varCount = 0, predicateCount = 0;
		Map<String, String> vars = new HashMap<String, String>();
		for (String item: items) vars.put(item, "S_" + varCount++ );
		for (String lp: labelProperties) vars.put(lp, "P_" + predicateCount++);
		
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {").append("\n");
		String dot = "";
		for (String r: items)
			for (String p: labelProperties) {			
				sb
					.append(dot)
					.append( " ?" ).append(vars.get(r))
					.append(" ?").append(vars.get(p))
					.append(" ?").append(vars.get(r)).append("_").append(vars.get(p))
					.append("\n")
					;
				dot = " . ";
			}			
	//
		sb.append("} WHERE {").append("\n");
		String union = "";
		for (String r: items) 
			for (String p: labelProperties) {
				sb
				.append( union )
				.append( " {" )
				.append(" BIND(<").append(r).append("> AS ?").append(vars.get(r)).append(")").append("\n")
				.append("  BIND(<").append(p).append("> AS ?").append(vars.get(p)).append(")").append("\n")
				.append( " ?" ).append(vars.get(r))
				.append(" ?").append(vars.get(p)).append(" ")
				.append(" ?").append(vars.get(r)).append("_").append(vars.get(p))
				.append( "}")
				.append("\n")
				;
				union = "UNION";
			}
		sb.append("}").append("\n");
		
		String sq = sb.toString();
    	
    	final Model m = ModelFactory.createModelForGraph(source.construct(sq));
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
        JSONWritable so = null;        
        final API_Dataset api = getAPI(dataset);
        try {
            DataQuery q = DataQueryParser.Do(p, api, query);
            log.info(flatten("Request: " + query.toString()));
            String sq = null;
            if (p.isOK()) {
                sq = q.toSparql(p, api);
                if (checkingSPARQL) checkLegalSPARQL(p, sq);
            }

            if (p.isOK()) {
            	// log.info("issuing query:\n" + sq);
            	log.info(flatten("issuing query:\n" + sq));
                SparqlSource source = api.getSource();
                log.info(">> retrieved SparqlSource");
                if (source instanceof RemoteSparqlSource) {
                	log.info(">> ... it's remote, setting content type.");
                	((RemoteSparqlSource) source).setContentType("tsv");
                	log.info(">> set the content type.");
                } else {
                	log.info(">> not remote source.");
                }
				ClosableResultSet ss = source.streamableSelect(sq);
				log.info(">> fetched streamable select");
				so = q.getWriter(api, ss);
				log.info(">> got writer");
            }

        } catch (Exception e) {
            log.error(flatten("BROKEN: " + e), e);
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
        	log.info(">> returning so.");
            return so;

        } else {
            String problemStrings = p.getProblemStrings();
            throw new WebApiException(Status.BAD_REQUEST, "FAILED:\n" + BunchLib.join(problemStrings));
        }
    }

    /**
     * <pre>base/dataset/{dataset}/explain</pre>
     * 
     * Return a summary of the translation of a query as a json object (not streaming)
     */
    public Response datasetExplainEndpoint(String dataset, JsonObject query) {
        API_Dataset api = getAPI(dataset);

        Problems p = new Problems();
        
        JsonObject comments = new JsonObject();
        
        String x = "[unknown]";
        SparqlSource s = api.getSource();
        if (s instanceof RemoteSparqlSource) {
        	RemoteSparqlSource rs = (RemoteSparqlSource) s;
        	x = rs.getEndpoint();
        }

        comments.put("sparqlQueryURL", api.getSparqlQueryURL(x));
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
            
            comments.put("sparql", "(No SPARQL generated)");

            if (p.isOK())
                q = DataQueryParser.Do(p, api, query);

            if (p.isOK()) {
                sq = q.toSparql(p, api);
            	// System.err.println( ">> issuing query:\n" + sq);
                boolean legal = isLegalSPARQL(p, sq);
				comments.put(
                	"sparql" 
                	, (legal ? QueryFactory.create(sq).toString() : "Bad SPARQL:\n" + sq + "\n" )
                	);
            }

            /*
            // This needs to be configurable but suppress for now
            if (p.isOK()) {
                long start = System.currentTimeMillis();
                ResultSet rs = source.select(sq);
                RowConsumer discard = new RowConsumer() {
					
					@Override public void consume(Row jv) {						
					}
				};
                ResultsToRows.convert(api.getAspects(), discard, rs);
                long finish = System.currentTimeMillis();
                comments.put("time", finish-start);
            }
            */

        } catch (Exception e) {
            log.error(flatten("BROKEN: " + e), e);
            p.add("BROKEN: " + e);
        }
        
        comments.put("status", p.isOK());
        comments.put("problems", p.getProblemStrings());
        
        return Response.ok(comments.toString()).build();
    }
    
    private String flatten(String toFlatten) {
    	return Switches.flattening ? toFlatten.replace('\n',  ' ') : toFlatten;
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
    
    private boolean isLegalSPARQL(Problems p, String sq) {
        try { 
        	QueryFactory.create(sq); 
        	return true; 
        } catch (Exception e) { 
            p.add("Bad generated SPARQL:\n" + sq + "\n" + e.getMessage());
            return false;
        }
    }

}
