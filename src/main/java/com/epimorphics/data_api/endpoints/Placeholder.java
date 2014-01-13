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

import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.reporting.Problems;

@Path( "/placeholder") public class Placeholder {

	@POST @Produces("text/plain") public Response placeholderPOST(String posted) {
		
		Problems p = new Problems();

		try {
			JsonObject jo = JSON.parse(posted);
			DataQuery q = DataQueryParser.Do(p, jo);
			
			if (p.size() > 0) {
				return Response.serverError().entity("Problems detected: " + p).build();
			}
			
			return Response.ok("OK (POST) : " + posted + "." + "\n" + q.filters() + ".\n").build();
		} catch (Exception e) {
			return Response.serverError().entity("Broken: " + e).build();
		}		
	}
	
	@GET @Produces("text/plain") public Response placeholderGET() { 
		return Response.ok("OK (GET).").build();
	}
}
