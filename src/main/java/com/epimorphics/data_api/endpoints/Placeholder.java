package com.epimorphics.data_api.endpoints;

import java.net.URISyntaxException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path( "/placeholder") public class Placeholder {

	@POST @Produces("text/plain") public Response placeholderPOST(String posted) throws URISyntaxException {
		return Response.ok("OK (POST) : " + posted + ".").build();
	}
	
	@GET @Produces("text/plain") public Response placeholderGET() throws URISyntaxException { 
		return Response.ok("OK (GET).").build();
	}
}
