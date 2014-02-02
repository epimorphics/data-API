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
import com.epimorphics.json.JsonUtil;
import com.hp.hpl.jena.shared.PrefixMapping;

// this should be data-driven, ie, a bunch of plugins that respond to
// operator names.
public class DataQueryParser {
	
	// these would be built up from the plugins. Probably use the appbase configure stuff.
	static final String opNames = "eq ne le lt ge gt contains matches search below oneof";
	
	static final Set<String> allowedOps = new HashSet<String>(Arrays.asList(opNames.split(" ")));
	
	public static DataQuery Do(Problems p, PrefixMapping pm, JsonObject jo) {
		if (jo.isObject()) {
			List<Filter> filters = new ArrayList<Filter>();
			List<Sort> sortby = new ArrayList<Sort>();
			
			for (String key: jo.getAsObject().keys()) {
				if (key.startsWith("_")) {
					if (key.equals("_sort")) {
						extractSorts(pm, p, jo, sortby, key);
					} else {
						p.add("unknown directive '" + key + "' in data query " + jo + ".");
					}
				} else {
					Shortname sn = new Shortname(pm, key);
					JsonValue range = jo.get(key);			
					if (range.isObject()) {
						JsonObject rob = range.getAsObject();
						for (String op: rob.keys()) {
							List<Term> v = DataQueryParser.jsonToTerms(p, pm, rob.get(op));
							if (isKnownOp(op)) {
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
			return new DataQuery(filters, sortby);
		} else {
			throw new RuntimeException("Error handling to be done here." );
		}
	}

	private static void extractSorts(PrefixMapping pm, Problems p, JsonObject jo, List<Sort> sortby, String key) {
		JsonValue x = jo.get(key);
		if (x.isArray()) {
			for (JsonValue candidate: x.getAsArray()) {
				if (candidate.isObject()) {
					JsonObject sort = candidate.getAsObject();
					String up = JsonUtil.getStringValue(sort, "@up", null);
					String down = JsonUtil.getStringValue(sort, "@down", null);
					if (up == null && down == null) {
						p.add("sort term " + sort + " has neither @up nor @down member.");
					} else if (up != null && down != null) {
						p.add("sort term " + sort + " has both @up and @down members.");
					} else {
						String name = (up == null ? down : up);
						Shortname sn = new Shortname(pm, name);
						Sort s = new Sort(sn, down == null);
						sortby.add(s);
					}
				} else {
					p.add("sort term " + candidate + " should be an object.");
				}
			}
		} else {
			p.add("value of _sort must be array, was given " + x + ".");
		}
	}

	private static boolean isKnownOp(String op) {
		return allowedOps.contains(op);
	}

	// TODO literals with a language
	public static Term jsonToTerm(Problems p, PrefixMapping pm, JsonValue jv) {
		if (jv.isBoolean()) return Term.bool(jv.getAsBoolean().value());
		if (jv.isString()) return Term.string(jv.getAsString().value());
		if (jv.isNumber()) return Term.number(jv.getAsNumber().value());
		if (jv.isObject()) {			
			JsonObject jo = jv.getAsObject();
			
			JsonValue id = jo.get("@id");
			
			if (id != null) {
				if (id.isString()) {
					return Term.URI( pm.expandPrefix(id.getAsString().value()) );
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

	public static List<Term> jsonToTerms(Problems p, PrefixMapping pm, JsonValue jv) {
		if (jv.isArray()) {
			List<Term> values = new ArrayList<Term>();
			for (JsonValue element: jv.getAsArray()) values.add(jsonToTerm(p, pm, element));
			return values;
		} else {
			return BunchLib.list(jsonToTerm(p, pm, jv));
		}
	}
}