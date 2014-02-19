/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.libs;

import java.util.List;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.reporting.Problems;

// TODO Error handling not written yet.
public class JSONLib {

	public static String getFieldAsString(Problems p, JsonObject r,	String key) {
		if (r.hasKey(key)) {
			JsonValue v = r.get(key);
			if (v.isString()) {
				return v.getAsString().value();
			} else {
				throw new RuntimeException("Error handling to be done here.");
			}			
		} else {
			throw new RuntimeException("Error handling to be done here.");
		}
	}
	
	public static List<JsonValue> getFieldAsArray(Problems p, JsonObject r, String key) {
		if (r.hasKey(key)) {
			JsonValue v = r.get(key);
			if (v.isArray()) {
				return v.getAsArray().subList(0, v.getAsArray().size() );
			} else {
				throw new RuntimeException("Error handling to be done here.");
			}	
		} else {
			throw new RuntimeException("Error handling to be done here.");
		}
	}

	/**
	    jsonArray(elements...) returns a JSON array with the given elements
	    in the same order.
	*/
	public static JsonArray jsonArray(JsonValue... elements) {
		JsonArray result = new JsonArray();
		for (JsonValue v: elements) result.add(v);
		return result;
	}

}
