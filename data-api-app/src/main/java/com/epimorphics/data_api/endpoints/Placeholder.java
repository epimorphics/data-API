/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.endpoints;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.conversions.Convert;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

// placeholder endpoint, has a fake setup rather than a proper
// configuration.
@Path( "placeholder") public class Placeholder {
	
	public static class Config {
		String filePath;
		
		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
		
	}

	static Model m = loadExampleModel();
	
	static Model loadExampleModel() {
		App a = AppConfig.getApp();
		Config c = (Config) a.getComponent("configA");
		String filePath = AppConfig.theConfig.expandFileLocation(c.filePath);
		
		System.err.println( ">> filePath: " + filePath );
		
		return FileManager.get().loadModel(filePath);
	}
	
	PrefixMapping pm = PrefixMapping.Factory
			.create()
			.setNsPrefixes(PrefixMapping.Extended)
			.setNsPrefixes(m)
			.lock();
	
	String egc = "http://epimorphics.com/public/vocabulary/games.ttl#";
	
	Aspects aspects = new Aspects()
		.include(new Aspect( RDF.getURI() + "type", new Shortname(pm, "rdf:type" )))
		.include(new Aspect( RDFS.getURI() + "label", new Shortname(pm, "rdfs:label" )))
		.include(new Aspect( egc + "players", new Shortname(pm, "egc:players" )))
		.include(new Aspect( egc + "pubYear", new Shortname(pm, "egc:pubYear" )))
		;
	
	@GET @Path("submit") @Produces("text/html") public Response submit
		() {
		String entity = BunchLib.join(
			"<html>"
			, "<head>"
			, "<title>data API placeholder query submitter</title>"
			, "</head>"
			, "<body>"
			, "<h1>submit JSON query</h1>"
			, "<form method='POST' action='/data-api/placeholder/dataset/name/data'>"
			, "<textarea cols='80' rows='20' name='json'>"
			, "</textarea>"
			, "<div><input type='submit' name='button' value='SUBMIT' /></div>"
			, "</form>"
			, "</body>"
			, "</html>"
			);
		return Response.ok(entity).build();
	}
	
	@POST @Path("dataset/{name}/data") @Produces("text/plain") public Response placeholderPOST
		(@PathParam("name") String datasetName, @FormParam("json") String posted) {
		
		Problems p = new Problems();
		List<String> comments = new ArrayList<String>();
	
		comments.add( "posted JSON:\n" + posted + "\n");
		
		JsonObject jo = null;
		DataQuery q = null;
		String sq = null;
		Query qq = null;
		
		try {
			jo = JSON.parse(posted);
			
			q = DataQueryParser.Do(p, pm, jo);
			
			if (p.size() == 0) sq = q.toSparql(p, aspects, pm);
			try {
				qq = QueryFactory.create(sq);
				comments.add("Generated SPARQL:\n\n" + qq );
			} catch (Exception e) {
				p.add("Bad generated SPARQL:\n" + sq + "\n" + e.getMessage());
			}
			
			List<String> vars = new ArrayList<String>();
			for (Aspect a: aspects.getAspects()) vars.add(a.asVar());
					
			if (p.size() == 0) {
				QueryExecution qe = QueryExecutionFactory.create( qq, m );
				ResultSet rs = qe.execSelect();
			
				JsonArray them = new JsonArray();
				while (rs.hasNext()) {
					// sb.append(rs.next()).append("\n");
					JsonObject row = Convert.toJson(vars, rs.next());
					them.add(row);
				}
				comments.add("resultset:\n\n" + them);
			}
			
		} catch (Exception e) {
			System.err.println("BROKEN: " + e);
			e.printStackTrace(System.err);
			p.add("BROKEN: " + e);
		}	
		
		if (p.size() == 0) {
				return Response.ok
					( "OK\n" 
					+ BunchLib.join(comments)		
					).build()
					;
			} else {
				return Response.ok
					( "FAILED:\n"
					+ BunchLib.join(comments)
					+ BunchLib.join(p.getProblemStrings())
					).build()
					;
			}	
	}
}
