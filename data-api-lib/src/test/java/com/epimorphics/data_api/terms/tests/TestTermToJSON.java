/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.terms.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.json.JSFullWriter;

public class TestTermToJSON {

	@Test public void testArrayTerm() {
		
		Term A = Term.integer("17");
		Term B = Term.string("hello");
		Term C = Term.bool(true);
		Term a = Term.array(BunchLib.list(A, B, C));
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JSFullWriter out = new JSFullWriter(os);
		a.writeTo(out);
		out.finishOutput();
		
		assertSmushedEquals("[17, \"hello\", true]", os.toString());
	}

	private String unspaced(String a) {
		return a.replaceAll("[ \n]+", "");
	}

	@Test public void testArrayTermsWithObjects() {
		
		Term A = Term.URI("eh:/A");
		Term B = Term.URI("eh:/B");
		Term C = Term.array(BunchLib.list(A, B));
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JSFullWriter out = new JSFullWriter(os);
		C.writeTo(out);
		out.finishOutput();

		assertSmushedEquals("[{\"@id\": \"eh:/A\"}, {\"@id\": \"eh:/B\"}]", os.toString());
	}

	@Test public void testArrayTermsWithLiterals() {
		
		Term A = Term.string("A");
		Term B = Term.string("B");
		Term C = Term.array(BunchLib.list(A, B));
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JSFullWriter out = new JSFullWriter(os);
		C.writeTo(out);
		out.finishOutput();

		assertSmushedEquals("[ \"A\", \"B\" ]", os.toString());
	}

	@Test public void testArrayTermsWithTypedLiterals() {
		
		Term A = Term.typed("A", "xsd:string");
		Term B = Term.typed("B", "xsd:string");
		Term C = Term.array(BunchLib.list(A, B));
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JSFullWriter out = new JSFullWriter(os);
		C.writeTo(out);
		out.finishOutput();

		assertSmushedEquals("[ {\"@value\": \"A\", \"@type\": \"xsd:string\"}, {\"@value\": \"B\", \"@type\": \"xsd:string\"} ]", os.toString());
	}

	@Test public void testArrayTermsWithLanguagedLiterals() {
		
		Term A = Term.languaged("A", "en");
		Term B = Term.languaged("B", "cy");
		Term C = Term.array(BunchLib.list(A, B));
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		JSFullWriter out = new JSFullWriter(os);
		C.writeTo(out);
		out.finishOutput();

		assertSmushedEquals("[ {\"@value\": \"A\", \"@lang\": \"en\"}, {\"@value\": \"B\", \"@lang\": \"cy\"} ]", os.toString());
	}
	
	private void assertSmushedEquals(String a, String b) {
		String ua = unspaced(a), ub = unspaced(b);
		if (!ua.equals(ub)) {
			System.err.println( ">> A: " + ua);
			System.err.println( ">> B: " + ub);
			assertEquals(a, b);
		}	
	}
	
	
}
