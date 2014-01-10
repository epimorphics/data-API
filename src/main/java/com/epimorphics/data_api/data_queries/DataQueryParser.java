/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.libs.JSONLib;
import com.epimorphics.data_api.reporting.Problems;

public class DataQueryParser {
	
	public static DataQuery Do(Problems p, JsonObject jo) {
		if (jo.isObject()) {
			List<Range> ranges = new ArrayList<Range>();
			for (String key: jo.getAsObject().keys()) {
				if (key.startsWith("_")) {
					throw new RuntimeException("Error handling to be done here.");
				} else {
					JsonValue range = jo.get(key);			
					if (range.isObject()) {
						JsonObject r = range.getAsObject();
						String op = JSONLib.getFieldAsString(p, r, "op");
						List<JsonValue> operands = JSONLib.getFieldAsArray(p, r, "operands");			
						if (op.equals("eq") && operands.size() == 1) {
							Value v = JSONLib.getAsValue(operands.get(0));
							ranges.add( Range.EQ(v) );
						} else {
							throw new RuntimeException("Error handling to be done here.");	
						}
					} else {
						throw new RuntimeException("Error handling to be done here.");
					}
				}
			}
			return new DataQuery(ranges);
		} else {
			throw new RuntimeException("Error handling to be done here." );
		}
	}
}