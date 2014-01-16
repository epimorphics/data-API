/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.endpoints;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;

@Path( "/placeholder") public class Placeholder {
	
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

	@POST @Produces("text/plain") public Response placeholderPOST(String posted) {
		
		Problems p = new Problems();
		
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefixes(PrefixMapping.Extended).lock();
		
		try {
			JsonObject jo = JSON.parse(posted);
			DataQuery q = DataQueryParser.Do(p, pm, jo);
			
			if (p.size() > 0) {
				return Response.serverError().entity("Problems detected: " + p).build();
			}
			
			String sq = q.toSparql(p, pm);
			if (p.size() > 0) {
				return Response.serverError().entity("Problems detected: " + p).build();
			}
			
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
			return Response.serverError().entity("Broken: " + e).build();
		}		
	}
	
	@GET @Produces("text/plain") public Response placeholderGET() { 
		return Response.ok("OK (GET).").build();
	}
}
