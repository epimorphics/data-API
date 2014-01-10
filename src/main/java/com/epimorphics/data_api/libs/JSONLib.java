/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.libs;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.data_queries.Range;
import com.epimorphics.data_api.data_queries.Slice;
import com.epimorphics.data_api.data_queries.Sort;
import com.epimorphics.data_api.data_queries.Value;
import com.epimorphics.data_api.reporting.Problems;

// Error handling not written yet.
public class JSONLib {

	public static class DataQuery {
		
		final List<Range> ranges;
		
		public DataQuery(List<Range> ranges) {
			this.ranges = ranges;
		}
		
		public List<Sort> sorts() {
			return new ArrayList<Sort>();
		}
		
		public List<Range> ranges() {
			return ranges;
		}
		
		public String lang() {
			return null;
		}
		
		public Slice slice() {
			return new Slice();
		}
	}

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

	public static Value getAsValue(JsonValue jv) {
		if (jv.isBoolean()) return Value.wrap(jv.getAsBoolean().value());
		if (jv.isString()) return Value.wrap(jv.getAsString().value());
		if (jv.isNumber()) return Value.wrap(jv.getAsNumber().value());
		throw new RuntimeException("Error handling to be done here.");
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

}
