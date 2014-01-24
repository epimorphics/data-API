/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.libs.JSONLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

public class DataQueryParser {
	
	static final Set<String> allowedOps = new HashSet<String>(Arrays.asList("eq ne le lt ge gt".split(" ")));
	
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
							if (isRelationalOp(op)) {
								filters.add( new Filter(sn, new Range(op, BunchLib.list(v)) ) );
							} else if (op.equals("oneof")) {
								filters.add( new Filter(sn, new Range(op, (List<Value>) v.wrapped)));
							} else if (op.equals("below")) {
								filters.add( new Filter(sn, new Range(op, BunchLib.list(v))));
							} else {
								p.add("unknown operator '" + op + "' in data query.");
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

	private static boolean isRelationalOp(String op) {
		return allowedOps.contains(op);
	}
}