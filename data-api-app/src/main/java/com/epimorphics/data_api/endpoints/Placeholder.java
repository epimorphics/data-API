/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Shortname;
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

@Path( "/dataset") public class Placeholder {
	
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
	
	@POST @Path("{name}/data") @Produces("text/plain") public Response placeholderPOST
		(@PathParam("name") String datasetName, String posted) {
		
		System.err.println( ">> placeholderPOST for " + datasetName + ":" );
		
		Problems p = new Problems();
		
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
		
		try {
			JsonObject jo = JSON.parse(posted);
			DataQuery q = DataQueryParser.Do(p, pm, jo);
			
			if (p.size() > 0) {
				return Response.serverError().entity("Problems detected: " + p).build();
			}
			
			String sq = q.toSparql(p, aspects, pm);
			if (p.size() > 0) {
				return Response.serverError().entity("Problems detected: " + p).build();
			}
			
			System.err.println( ">> SQ:\n" + sq + "\n" );
			
			Query qq = QueryFactory.create(sq);
			QueryExecution qe = QueryExecutionFactory.create( qq, m );
			ResultSet rs = qe.execSelect();
			
			StringBuilder sb = new StringBuilder();
			while (rs.hasNext()) sb.append(rs.next()).append("\n");
			
			return Response.ok
				( "OK (POST) : " + posted + "." 
				+ "\n" + qq.toString() 
				+ ".\n resultset:"
				+ "\n" + sb.toString() 
				+ "\n"
				).build();
			
		} catch (Exception e) {
			System.err.println("BROKEN: " + e);
			e.printStackTrace(System.err);
			return Response.serverError().entity("Broken: " + e).build();
		}		
	}
	
	@GET @Produces("text/plain") public Response placeholderGET() { 
		return Response.ok("OK (GET).").build();
	}
}
