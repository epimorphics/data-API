/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		stackTraceIfDebugging(e);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
			.entity(e.toString())
			.build()
			;
	}

	/**
	    stackTraceIfDebugging logs a debug stacktrace of the exception e
	    if debug logging is enabled. Otherwise it does nothing.
	*/
	public static void stackTraceIfDebugging(Exception e) {
		if (log.isDebugEnabled()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(bos);
			e.printStackTrace(ps);
			ps.flush();
			log.debug(bos.toString());
		}
	}

}