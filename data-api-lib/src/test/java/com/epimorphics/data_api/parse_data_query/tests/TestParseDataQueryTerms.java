/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.parse_data_query.tests;

import static org.junit.Assert.*;

import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Term;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestParseDataQueryTerms {

	@Test public void testURI() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Extended;
		JsonObject jo = new JsonObject();
		jo.put("@id", "http://example.org/path");
		Term t = DataQueryParser.jsonToTerm(p, pm, jo);
		assertEquals(Term.URI("http://example.org/path"), t);
	}
	
	@Test public void testTyped() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Extended;
		JsonObject jo = new JsonObject();
		jo.put("@value", "1066");
		jo.put("@type", "xsd:integer");
		Term t = DataQueryParser.jsonToTerm(p, pm, jo);
		assertEquals(Term.typed("1066", "xsd:integer"), t);
	}
	
	@Test public void testLanguaged() {
		Problems p = new Problems();
		PrefixMapping pm = PrefixMapping.Extended;
		JsonObject jo = new JsonObject();
		jo.put("@value", "chat");
		jo.put("@lang", "fr");
		Term t = DataQueryParser.jsonToTerm(p, pm, jo);
		assertEquals(Term.languaged("chat", "fr"), t);
	}

}
