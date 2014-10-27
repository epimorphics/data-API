/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.conversions;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.data.ClosableResultSet;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.libs.MutableBool;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.query.ResultSet;

/**
    A RowWriter is initialised with a ResultSet 
    and incrementally renders that ResultSet as JSON.
    The heavy lifting is done by resultsToRows.
<p>
	We do our best to trap and report errors. The typical
	(but not the only) error is unexpected end-of-stream from
	the underlying ResultSet. Errors arising from the
	setup of the RowWriter are (expected to be) caught by
	the general ExceptionMapper in LogExceptions.
<p>
	When the RowWriter's writeTo(jw) is called it invokes
	the ResultsToRwows.convert method, which will repeatedly
	pull ResultSet rows from the SPARQL endpoint, massage them into
	JSON Row objects, and drop those into jw's writeTo method.
	There are three possible exception paths: when pulling 
	a SPARQL row, when pushing a Row, and the "impossible" anything 
	else.
<p>
	An exception thrown when the Row is being created (not written)
	is logged and a "poisoned" JSON row is written. This row has
	at least the member @poison with value some descriptive string;
	other members may be present. Clients pulling rows are expected
	to handle @poison or to fail because a poisoned row is not
	well-formed as a DSAPI result object.
<p>
	Exceptions thrown when the JSON Row object is being serialised
	are also logged. Poisoning is harder to accomplish since we may
	be part-way through an output token but we still try. The
	exception is wrapped in a caught exception and rethrown so
	that it can be caught in the body of writeTo but not logged
	for a second time.
<p>
	Any exception that slips through the catches for the previous
	two is also logged and poisoned. This should never happen.
<p>
	In any case, if the result set is closable it is specifically
	closed, so that if we're not at the natural end-of-results
	the result pipeline is closed down cleanly.
*/
public final class RowWriter implements JSONWritable {

    static Logger log = LoggerFactory.getLogger(RowWriter.class);
    
	private final ResultSet rs;
	private final ResultsToRows rtr;

	/**
	    Initialise this RowWriter with the ResultSet rs it will
	    consume and the aspects and compactions required by the
	    ResultsToRows it will construct and run.
	*/
	public RowWriter(Set<Aspect> aspects, ResultSet rs, Compactions c) {
		this.rs = rs;
		this.rtr = new ResultsToRows(aspects, c);
	}

	/**
	    Wrapper round exceptions to say that they have already been
	    caught and logged and do not have to be re-logged.
	 */
	static class Caught extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		public Caught(Exception e) {
			super(e);
		}
	}
	
	/**
	    writeTo(jw) is called for this RowWriter to write the
	    JSON form of the results from rs to the JSFullWriter
	    jw. Exceptions are caught and logged and, if possible,
	    the output stream poisoned so that clients can recognise
	    that an error occurred after the `200 OK` response was 
	    generated.
	*/
	@Override public void writeTo(final JSFullWriter jw) {
	    try {
    		final MutableBool comma = new MutableBool();
    		jw.startArray();
    		
    		/**
    		    RowConsumer stream consumes JSON Row objects
    		    by serialising their JSON form to jw.
    		*/
    		RowConsumer stream = new RowConsumer() {
    			
    			@Override public void consume(Row jv) {
    				try {
	    				if (comma.value) jw.arraySep(); 
	    				jv.writeTo(jw);
	    				comma.value = true;
    				} catch (Exception e) {
    					log.error("error writing JSON: " + e.getMessage());
    					throw new Caught(e);
    				}
    			}
    		};
    		
    		/**
    		    writeTo(jw) requests the ResultsToRows object rtr to
    		    convert the rows from the ResultSet rs to their
    		    serialised JSON form.
    		*/
    		try { 
    			rtr.convert(stream, rs); 
    		} catch (Caught e) {
    			stream.consume(poison(e));
    		} catch (Exception e) {
    			stream.consume(poison(e));
    			log.error("Failure: " + e.getMessage());
    		}
    		
    		jw.finishArray();
    		jw.finishOutput();
    		
	    } catch (Exception e) {
	    	log.error("unhandled exception in RowWriter: " + e.getMessage());
	    	poison(jw, e);
	    }
	    finally {
	        if (rs instanceof ClosableResultSet) {
	            ((ClosableResultSet)rs).close();
	        }
	    }
	}

	private void poison(JSFullWriter jw, Exception e) {
		jw.startObject();
		jw.pair("@poison", e.getMessage());
		jw.finishObject();
	}

	private Row poison(Exception e) {
		return new Row().put("@poison", Term.string(e.getMessage()));
	}
}

