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
*/
public final class RowWriter implements JSONWritable {

    static Logger log = LoggerFactory.getLogger(RowWriter.class);
    
	private final ResultSet rs;
	private final ResultsToRows rtr;

	public RowWriter(Set<Aspect> aspects, ResultSet rs, Compactions c) {
		this.rs = rs;
		this.rtr = new ResultsToRows(aspects, c);
	}

	static class Caught extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		public Caught(Exception e) {
			super(e);
		}
	}
	
	@Override public void writeTo(final JSFullWriter jw) {
	    try {
    		final MutableBool comma = new MutableBool();
    		jw.startArray();
    		
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

