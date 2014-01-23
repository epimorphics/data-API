/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonBoolean;
import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.libs.JSONLib;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Convert {

	/**
	    toJson(n) returns the data API's JSON representation for
	    the RDF Node n.
	*/
	public static JsonValue toJson(Node n) {
		if (n.isURI()) 
			return Convert.objectWith("@id", n.getURI());
		if (n.isLiteral()) {
			String spelling = n.getLiteralLexicalForm();
			String type = n.getLiteralDatatypeURI();
			if (type == null) {
				String language = n.getLiteralLanguage();
				if (language.equals("")) {
					return new JsonString(spelling); // Convert.objectWith("@value", spelling);
				} else {
					return Convert.objectWith("@value", spelling, "@language", language);
				}
			} else if (type.equals(XSDDatatype.XSDboolean.getURI())) {
				return new JsonBoolean(spelling.equals("true"));
			} else if (type.equals(XSDDatatype.XSDinteger.getURI())) {
				return JsonNumber.valueInteger(spelling);
			} else if (type.equals(XSDDatatype.XSDdecimal.getURI())) {
				return JsonNumber.valueDecimal(spelling);				
			} else if (type.equals(XSDDatatype.XSDfloat.getURI())) {
				return JsonNumber.valueDouble(spelling);				
			} else if (type.equals(XSDDatatype.XSDdouble.getURI())) {
				return JsonNumber.valueDouble(spelling);
			} else if (type.equals(XSDDatatype.XSDint.getURI())) {
				return JsonNumber.valueInteger(spelling);
			} else {
				return Convert.objectWith("@type", type, "@value", spelling);
			}
		}
		throw new RuntimeException("cannot handle this node: " + n);
	}

	public static JsonObject objectWith(Object... bindings) {
		JsonObject job = new JsonObject();
		for (int i = 0; i < bindings.length; i += 2) {
			String key = (String) bindings[i];
			Object value = bindings[i+1];
			if (value instanceof String) job.put(key, (String) value);
			else if (value instanceof JsonValue) job.put(key,  (JsonValue) value);
			else throw new RuntimeException("Unexpected as value for member: " + value);
		}
		return job;
	}

	static final JsonArray emptyArray = new JsonArray();
	
	/**
	    toJson(vars, qs) returns the data API JSON representation of
	    a ARQ query solution restricted to the named vars.
	*/
	public static JsonObject toJson(List<Aspect> aspects, QuerySolution qs) {
		
//		System.err.println( ">> qs: " );
//		Iterator<String> it = qs.varNames();
//		while (it.hasNext()) System.err.print( " " + it.next() );
//		System.err.println();
		
		JsonObject result = new JsonObject();
		for (Aspect a: aspects) {
			String key = a.getName().getCURIE();
			
			System.err.println( ">> asVar: " + a.asVar() );
			
			RDFNode value = qs.get(a.asVar());
			
			// System.err.println( ">> value of " + var + " is " + value );
			
			if (value == null) {
				result.put(key, emptyArray);				
			}
			else {
				JsonValue v = toJson(value.asNode());
				result.put(key, (a.getIsMultiValued() ? JSONLib.jsonArray(v) : v));
			}
		}
		return result;
	}

}
