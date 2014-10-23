/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Provider 
public class LogExceptions implements ExceptionMapper<Exception>{

	static final Logger log = LoggerFactory.getLogger(LogExceptions.class);
	
	public LogExceptions() {
	}
	
	@Override public Response toResponse(Exception e) {
		log.error("FAILED: " + e.toString(), e);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
			.entity(e.toString())
			.build()
			;
	}

}