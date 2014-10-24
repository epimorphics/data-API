/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import java.io.PrintStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

/**
    LogExceptions is an ExceptionMapper that maps every exception
    to a report repose and a log entry, except that WebApplicationExceptions
    are not logged.
*/
@Provider public class LogExceptions implements ExceptionMapper<Exception>{

	static final Logger log = LoggerFactory.getLogger(LogExceptions.class);
	
	public LogExceptions() {
	}
	
	/**
	    toResponse(e) logs that e happened and builds a response that
	    reports that. If the log has debug enabled, then e's stack trace
	    is also logged.	If e is a WebApplicationException than toResponse
	    returns e's getResponse().
	*/
	@Override public Response toResponse(Exception e) {
		if (e instanceof WebApplicationException) return ((WebApplicationException) e).getResponse();
		log.error("FAILED: " + e.toString(), e);
		if (log.isDebugEnabled()) log.debug(stackTrace(e));
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
			.entity(e.toString())
			.build()
			;
	}

	/**
	    stackTrace returns a string representation of the stack
	    trace of the exception e.
	*/
	private String stackTrace(Exception e) {
		ByteOutputStream bos = new ByteOutputStream();
		PrintStream ps = new PrintStream(bos);
		e.printStackTrace(ps);
		ps.flush();
		return bos.toString();
	}

}