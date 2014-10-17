/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.conversions;

import java.util.Set;

import com.epimorphics.appbase.data.ClosableResultSet;
import com.epimorphics.data_api.aspects.Aspect;
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
	
	private final ResultSet rs;
	private final ResultsToRows rtr;

	public RowWriter(Set<Aspect> aspects, ResultSet rs, Compactions c) {
		this.rs = rs;
		this.rtr = new ResultsToRows(aspects, c);
	}

	@Override public void writeTo(final JSFullWriter jw) {
	    try {
    		final MutableBool comma = new MutableBool();
    		jw.startArray();
    		
    		RowConsumer stream = new RowConsumer() {
    			
    			@Override public void consume(Row jv) {
    				if (comma.value) jw.arraySep(); 
    				jv.writeTo(jw);
    				comma.value = true;
    			}
    		};
    		rtr.convert(stream, rs);
    		jw.finishArray();
    		jw.finishOutput();
	    } catch (Exception e) {
	        if (rs instanceof ClosableResultSet) {
	            ((ClosableResultSet)rs).close();
	        }
	        throw e;
	    }
	}
}