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
import com.hp.hpl.jena.shared.PrefixMapping;

public class DataQueryParser {
	
	public static DataQuery Do(Problems p, PrefixMapping pm, JsonObject jo) {
		if (jo.isObject()) {
			List<Filter> filters = new ArrayList<Filter>();
			for (String key: jo.getAsObject().keys()) {
				if (key.startsWith("_")) {
					throw new RuntimeException("Error handling to be done here.");
				} else {
					Shortname sn = new Shortname(pm, key);
					JsonValue range = jo.get(key);			
					if (range.isObject()) {
						JsonObject rob = range.getAsObject();
						
						for (String op: rob.keys()) {
							Value v = JSONLib.getAsValue(rob.get(op));
							if (op.equals("eq")) {
								filters.add( new Filter(sn, Range.EQ(v) ) );
								
							} else {
								throw new RuntimeException("Error handling to be done here.");	
							}
						}
						
					} else {
						throw new RuntimeException("Error handling to be done here.");
					}
				}
			}
			return new DataQuery(filters);
		} else {
			throw new RuntimeException("Error handling to be done here." );
		}
	}
}