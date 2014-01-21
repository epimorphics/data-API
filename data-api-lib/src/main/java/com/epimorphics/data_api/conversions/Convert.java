/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import org.apache.jena.atlas.json.JsonBoolean;
import org.apache.jena.atlas.json.JsonNumber;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

public class Convert {

	public static JsonValue toJson(Node n) {
		if (n.isURI()) 
			return Convert.objectWith("@id", n.getURI());
		if (n.isLiteral()) {
			String spelling = n.getLiteralLexicalForm();
			String type = n.getLiteralDatatypeURI();
			if (type == null) {
				String language = n.getLiteralLanguage();
				if (language.equals("")) {
					return Convert.objectWith("@value", spelling);
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

	public static JsonObject objectWith(String ...strings) {
		JsonObject job = new JsonObject();
		for (int i = 0; i < strings.length; i += 2) {
			String key = strings[i], value = strings[i+1];
			job.put(key, value);
		}
		return job;
	}

}
