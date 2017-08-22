/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.end2end.tests;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;

public class QueryTestSupport {

	/**
	<p>
	    Run the DS API JSON query in <code>queryString</code> over the
	    data and API config defined in <code>man</code>. The JSON results
	    should be compatible with those in <code>expectString</code>.
	</p>
	<p>
		By "compatible" we mean equal except that arrays are treated as
		sets, ie, the order of their elements is irrelevant. DS API
		results only use arrays for (a) the collection of result sets
		and (b) the representation of optional and/or multiple results
		for some aspect.
    </p>
	*/
	protected static void testQueryReturnsExpectedResults
		(DSAPIManager man, String queryString, String expectString) {
		JsonObject query = JSON.parse(queryString);
		JsonValue expectJSON = JSON.parse("{'array': " + expectString + " }").getAsObject().get("array");
		
		JSONWritable response = man.datasetDataEndpoint(null, "query-testing-dataset", query);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JSFullWriter jw = new JSFullWriter(bos) ;
		response.writeTo(jw);
		
		String results = bos.toString();
		String objectified = "{'array': " + results + "}\n";
		JsonObject jo = JSON.parse(objectified);
		JsonValue jv = jo.get("array");
		
		Object expectObject = quasiCopyConvertingArraysToSets(expectJSON);
		Object resultObject = quasiCopyConvertingArraysToSets(jv);
		
//		System.err.println("\n>> JSON query:" + queryString);
//		System.err.println("\n>> " + expectObject);
//		
//		System.err.println( "\n>> expectObject:\n" + nicely(expectObject).replaceAll( "http://www.epimorphics.com/test/dsapi/sprint3/", "s3:" ) );
//		System.err.println( "\n>> resultObject:\n" + nicely(resultObject ).replaceAll( "http://www.epimorphics.com/test/dsapi/sprint3/", "s3:" ) );
					
		assertEquals(expectObject, resultObject);
	}
	
	/**
	    Copy the JSON into a similar tree structure where JSONObjects
	    become maps and JSONArrays become sets. This allows the equality
	    test to ignore ordering within arrays, and since our results
	    all use arrays to represent sets, that's OK. 
	*/
	protected static Object quasiCopyConvertingArraysToSets(JsonValue jv) {
		if (jv.isArray()) {
			Set<Object> result = new HashSet<Object>();
			JsonArray ja = jv.getAsArray();
			for (int i = 0; i < ja.size(); i += 1) {
				result.add(quasiCopyConvertingArraysToSets(ja.get(i)));
			}
			return result;		
		} else if (jv.isObject()) {
			JsonObject jo = jv.getAsObject();
			Map<String, Object> new_jo = new HashMap<String, Object>();
			for (Map.Entry<String, JsonValue> e: jo.entrySet()) {
				new_jo.put(e.getKey(), quasiCopyConvertingArraysToSets(e.getValue()));
			}			
			return new_jo;
		} else {
			return jv;
		}
	}

	@SuppressWarnings("unchecked") protected static String nicely(Object x) {
		if (x instanceof Set) {
			StringBuilder result = new StringBuilder();
			for (Object element: ((Set<? extends Object>) x)) {
				result.append( nicely( element ) ).append("\n");
			}
			return result.toString();
		} else {
			return x.toString();
		}
	}

	public static String BLOCK(String... elements) {
		return BunchLib.join("{", BunchLib.join(elements), "}");
	}

	public static Set<Set<ResultBinding>> parseRows(String rows) {
		Set<Set<ResultBinding>> result = new HashSet<Set<ResultBinding>>();
		
		String scan = rows;
		while(true) {
			int semi = scan.indexOf(';');
			if (semi < 0) break;
			String row = scan.substring(0, semi).trim();			
			result.add(parseRow(row));
			scan = scan.substring(semi + 1);
		}
		result.add(parseRow(scan));
		
		return result;
	}

	public static Set<ResultBinding> parseRow(String row) {		
		Set<ResultBinding> result = new HashSet<ResultBinding>();
		for (String element: row.trim().split("[ \n]+")) {
			result.add(ResultBinding.parseBinding(element));
		}		
		return result;
	}
}
