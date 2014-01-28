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
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

// this should be data-driven, ie, a bunch of plugins that respond to
// operator names.
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
							List<Term> v = DataQueryParser.jsonToTerms(p, rob.get(op));
							if (isRelationalOp(op)) {
								filters.add( new Filter(sn, new Range(op, v) ) );
							} else if (op.equals("oneof")) {
								filters.add( new Filter(sn, new Range(op, v) ) );
							} else if (op.equals("below")) {
								filters.add( new Filter(sn, new Range(op, v) ) );
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

	public static Term jsonToTerm(Problems p, JsonValue jv) {
		if (jv.isBoolean()) return Term.bool(jv.getAsBoolean().value());
		if (jv.isString()) return Term.string(jv.getAsString().value());
		if (jv.isNumber()) return Term.number(jv.getAsNumber().value());
		if (jv.isObject()) {			
			JsonObject jo = jv.getAsObject();
			
			JsonValue id = jo.get("@id");
			
			if (id != null) {
				if (id.isString()) {
					return Term.URI(id.getAsString().value());
				} else {
					p.add("Cannot convert JSON value '" + jv + "' to Term: @id has a non-string value.");
					return Term.bad(jv);
				}
			}
			
			JsonValue value = jo.get("@value");
			JsonValue type = jo.get("@type");
			
			String typeString = type.getAsString().value();
			String valueString = value.getAsString().value();
		//
			return Term.typed(valueString, typeString);
		}
		p.add("Cannot convert JSON value '" + jv + "' to Term.");
		return Term.bad(jv);
	}

	public static List<Term> jsonToTerms(Problems p, JsonValue jv) {
		if (jv.isArray()) {
			List<Term> values = new ArrayList<Term>();
			for (JsonValue element: jv.getAsArray()) values.add(jsonToTerm(p, element));
			return values;
		} else {
			return BunchLib.list(jsonToTerm(p, jv));
		}
	}
}