/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints.support;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.conversions.ResultsToJson;
import com.epimorphics.data_api.conversions.ResultsToJson.JSONConsumer;
import com.hp.hpl.jena.query.ResultSet;

/**
    A StreamFromResults is a StreamingOutput layer over the
    JSON results from a multi-valued handling ResultSet.
*/
public final class StreamFromResults implements StreamingOutput {
	
	private final Set<Aspect> aspects;
	private final ResultSet rs;

	public StreamFromResults(Set<Aspect> aspects, ResultSet rs) {
		this.aspects = aspects;
		this.rs = rs;
	}

	@Override public void write(OutputStream output) throws IOException, WebApplicationException {
		final PrintStream ps = new PrintStream( output );
		final Bool comma = new Bool();
		
		ps.println( "[" );
		JSONConsumer stream = new JSONConsumer() {
			
			@Override public void consume(JsonValue jv) {
				if (comma.value) ps.print( ", " ); 
				ps.println(jv.toString());
				comma.value = true;
			}
		};
		ResultsToJson.convert(aspects, stream, rs);
		ps.println( "]" );
		
		ps.flush();
	}
    
    static final class Bool {
    	boolean value;
    }
}