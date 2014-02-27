/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
		DataQuery result = DoQuietly(p, dataset, jv);
		// System.err.println( ">> " + jv + "\n]] " + Composition.allToSparql(result.c));
		return result;
	}

	private static DataQuery DoQuietly(Problems p, API_Dataset dataset, JsonValue jv) {
		if (jv.isObject()) {
			DataQueryParser qp = new DataQueryParser(p, dataset);
			DataQuery q = qp.parseDataQuery(jv.getAsObject());
			// showBooleans(qp.booleans, 0);
			return q;
		} else {
			p.add("DataQuery should be a JSON object, but given: " + jv);
			return null;
		}
	}

	static void showBooleans(Map<String, Set<List<Filter>>> booleans, int depth) {
		for (String key: "@and/@or/@not".split("/")) {
			for (int i = 0; i < depth * 2; i += 1) System.err.print( " |");
			System.err.print(key);
			for (List<Filter> s: booleans.get(key)) {
				System.err.print(s);
			}
			System.err.println();
		}
	}
	
	final Set<String> aspectURIs = new HashSet<String>();
	final List<Filter> filters = new ArrayList<Filter>();
	final List<Sort> sortby = new ArrayList<Sort>();
	final List<Guard> guards = new ArrayList<Guard>();
	
	final Map<String, List<Composition>> compositions = new HashMap<String, List<Composition>>();

	String globalSearchPattern = null;
	Integer length = null, offset = null;
	
	final Problems p;
	final API_Dataset dataset;
	final PrefixMapping pm;
	
	DataQueryParser(Problems p, API_Dataset dataset) {
		this.p = p;
		this.dataset = dataset;
		this.pm = dataset.getPrefixes();
	//
		for (Aspect a: dataset.getAspects()) aspectURIs.add(a.getID());
		compositions.put("@or", new ArrayList<Composition>() );
		compositions.put("@and", new ArrayList<Composition>() );
		compositions.put("@not", new ArrayList<Composition>() );
	}

	private DataQuery parseDataQuery(JsonObject jo) {		
		for (String key: jo.keys()) {
			JsonValue value = jo.get(key);
			if (key.startsWith("@")) {
				parseAtMembers(jo, key, value);
			} else {
				parseAspectMembers(jo, key, value);
			}
		}
//		compositions.get("@and").add(filters);
		Composition c = Composition.build(filters, compositions);
		return new DataQuery(c, sortby, guards, Slice.create(length, offset), globalSearchPattern);
	}

	private void parseAspectMembers(JsonObject jo, String key, JsonValue range) {
		Shortname sn = new Shortname(pm, key);
		if (!aspectURIs.contains(sn.URI)) {
			p.add("Unknown shortname '" + key + "' in " + jo );
		} else {			
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

	private void parseAtMembers(JsonObject jo, String key, JsonValue value) {
		if (key.equals("@sort")) {
			extractSorts(pm, p, jo, sortby, key);
		} else if (key.equals("@search")) {
			globalSearchPattern = extractString(p, key, value);
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
		} else if (key.equals("@and") || key.equals("@or") || key.equals("@not")) {
			
			List<Composition> these = compositions.get(key);
			
			if (value.isArray()) {
				for (JsonValue element: value.getAsArray()) {
					DataQuery subQuery = DoQuietly(p, dataset, element);
					these.add(subQuery.c);
				}			
			} else {
				p.add("operand of " + key + " must be an array: " + value );
			}
			
		} else {
			p.add("unknown directive '" + key + "' in data query " + jo + ".");
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

	private static String extractString(Problems p, String key, JsonValue value) {
		if (value.isString()) return value.getAsString().value();
		p.add("value of " + key + " must be string, given: " + value);
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