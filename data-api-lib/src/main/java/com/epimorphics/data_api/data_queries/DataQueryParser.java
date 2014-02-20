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

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.JSONConstants;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.json.JsonUtil;
import com.hp.hpl.jena.shared.PrefixMapping;

// this should be data-driven, ie, a bunch of plugins that respond to
// operator names.
public class DataQueryParser {
	
	// these would be built up from the plugins. Probably use the appbase configure stuff.
	static final String opNames = "eq ne le lt ge gt contains matches search below oneof childof in";
	
	static final Set<String> allowedOps = new HashSet<String>(Arrays.asList(opNames.split(" ")));
    
	public static DataQuery Do(Problems p, API_Dataset dataset, JsonValue jv) {
		
		PrefixMapping pm = dataset.getPrefixes();
		Set<String> aspectURIs = new HashSet<String>();
		for (Aspect a: dataset.getAspects()) aspectURIs.add(a.getID());
		
		if (jv.isObject()) {
			Integer length = null, offset = null;
			JsonObject jo = jv.getAsObject();
			List<Filter> filters = new ArrayList<Filter>();
			List<Sort> sortby = new ArrayList<Sort>();
			List<Guard> guards = new ArrayList<Guard>();
			
			for (String key: jv.getAsObject().keys()) {
				JsonValue value = jv.getAsObject().get(key);
				if (key.startsWith("@")) {
					if (key.equals("@sort")) {
						extractSorts(pm, p, jo, sortby, key);
					} else if (key.equals("@limit")) {
						length = extractNumber(p, key, value);
					} else if (key.equals("@offset")) {
						offset = extractNumber(p, key, value);
					} else if (key.equals(JSONConstants.CHILDOF)) {
					    if (dataset == null || !dataset.isHierarchy()) {
					        p.add("Tried to use @childof on a dataset that isn't a code list");
					    } else {
					        guards.add( new ChildofGuard(jsonResourceToTerm(p, pm, value), dataset.getHierarchy()) );
					    }
					} else {
						p.add("unknown directive '" + key + "' in data query " + jv + ".");
					}
				} else {
					Shortname sn = new Shortname(pm, key);
					if (!aspectURIs.contains(sn.URI)) {
						p.add("Unknown shortname '" + key + "' in " + jv );
					} else {
						JsonValue range = jo.get(key);			
						if (range.isObject()) {
							JsonObject rob = range.getAsObject();
							for (String opKey: rob.keys()) {
								if (opKey.startsWith("@")) {
									String op = opKey.substring(1);
									List<Term> v = DataQueryParser.jsonToTerms(p, pm, rob.get(opKey));
									if (isKnownOp(op)) {
										filters.add( new Filter(sn, new Range(op, v) ) );
									} else {
										p.add("unknown operator '" + op + "' in data query.");
									}
								} else {
									p.add("illegal member " + opKey);
								}
							}
							
						} else {
							p.add("Value of shortname '" + key + "' should be Object, given " + range);
						}
					}
				}
			}
			return new DataQuery(filters, sortby, guards, Slice.create(length, offset));
		} else {
			throw new RuntimeException("Error handling to be done here." );
		}
	}

	private static Integer extractNumber(Problems p, String key, JsonValue value) {
		if (value.isNumber()) {
			int n = value.getAsNumber().value().intValue();
			if (n >= 0) return new Integer(n);
		} 
		p.add("value of " + key + " must be non-negative number: " + value);
		return null;
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
			p.add("value of @sort must be array, was given " + x + ".");
		}
	}

	private static boolean isKnownOp(String op) {
		return allowedOps.contains(op);
	}
	
	public static Term jsonResourceToTerm(Problems p, PrefixMapping pm, JsonValue jv) {
	    if (jv.isNull()) {
	        return null;
	    } else if (jv.isString()) {
	        return Term.URI( pm.expandPrefix(jv.getAsString().value()) );
	    } else if (jv.isObject()) {
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
	    } 
	    
        p.add("Cannot convert JSON value '" + jv + "' to Term.");
        return Term.bad(jv);
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