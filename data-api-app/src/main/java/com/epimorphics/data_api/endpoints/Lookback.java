/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.endpoints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.epimorphics.data_api.libs.BunchLib;

/**
    A Lookback records information about a query and its results
    so that it can be fetched if the querier requires it.
*/
public class Lookback {
	
	/**
	    The order that the items are rendered in. Any remaining
	    items are rendered in pseudo-random order after the
	    keyOrdered items.
	*/
	static final List<String> keyOrder = BunchLib.list
		( "Dataset name"
		, "JSON-coded query"
		, "Generated SPARQL"
		, "Problems detected"
		);
	
	/**
	    The remarks are a map from tags of comments to those comments.
	 */
	protected Map<String, String> remarks = new HashMap<String, String>();
	
	public Lookback() {
	}
	
	/**
	    Add a comment under a specified key. Only one comment should be
	    added for a given key. (May change this later.)
	 */
	public Lookback addComment(String key, String comment) {
		remarks.put(key, comment);
		return this;
	}
	
	/**
	    Render this Lookback as plain text. 
	*/
	public String toText() {
		String gap = "";
		Set<String> remaining = new HashSet<String>( remarks.keySet() );
		StringBuilder sb = new StringBuilder();
		for (String key: keyOrder) {
			sb.append(gap); gap = "\n\n";
			sb.append( key ).append(":\n\n");
			sb.append(remarks.get(key));
			remaining.remove(key);
		}
		for (String key: remaining) {
			sb.append(gap); gap = "\n\n";
			sb.append( key ).append(":\n\n");
			sb.append(remarks.get(key));			
		}
		return sb.toString();
	}
	
}