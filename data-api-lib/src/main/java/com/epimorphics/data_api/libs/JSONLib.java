/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.libs;

import java.util.List;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.data_queries.Value;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.impl.LiteralLabel;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;

// Error handling not written yet.
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

	public static Value getAsValue(JsonValue jv) {
		if (jv.isBoolean()) return Value.wrap(jv.getAsBoolean().value());
		if (jv.isString()) return Value.wrap(jv.getAsString().value());
		if (jv.isNumber()) return Value.wrap(jv.getAsNumber().value());
		if (jv.isObject()) {			
			JsonObject jo = jv.getAsObject();
			JsonValue value = jo.get("@value");
			JsonValue type = jo.get("@type");
			LiteralLabel ll = LiteralLabelFactory.create(value.getAsString().value(), "", new BaseDatatype(type.getAsString().value()));
			Node n = NodeFactory.createLiteral(ll);
			System.err.println( ">> created literal node " + n );
			return Value.wrap(n);
		}
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