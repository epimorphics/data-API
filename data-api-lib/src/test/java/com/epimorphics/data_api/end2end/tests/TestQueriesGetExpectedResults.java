/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.end2end.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.libs.BunchLib;

//
// TODO (here or elsewhere)
// oneof length offset 
// contains matches
// search
//

public class TestQueriesGetExpectedResults {

	App testapp;
    DSAPIManager man;
    
    @Before public void startup() throws IOException  {
		testapp = new App("testapp", new File("src/test/data/query-testing/test.conf"));
        man = testapp.getComponentAs("dsapi", DSAPIManager.class);
    }
    
    String allExpected = BunchLib.join
		( "["
		, "  {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A-resource'}]"
		, "  , 'eg:value': 17"
		, "  , 'eg:values': [17, 18, 19]"
		, "  , 'eg:label': [{'@lang': 'cy', '@value': 'A'}, 'A-one', 'A1']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B-resource'}]"
		, "  , 'eg:value': 18"
		, "  , 'eg:values': [42, 43]"
		, "  , 'eg:label': ['B-one', 'B1']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C-resource'}]"
		, "  , 'eg:value': 19"
		, "  , 'eg:values': [99]"
		, "  , 'eg:label': ['C-one', 'C1']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
		, "  , 'eg:value': 20"
		, "  , 'eg:values': [42, 43]"
		, "  , 'eg:label': ['D-two', 'D2']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
		, "  , 'eg:value': 21"
		, "  , 'eg:values': [42, 99]"
		, "  , 'eg:label': ['E', 'e']"
		, "  }"
		, ", {"
		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F'"
		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F-resource'}]"
		, "  , 'eg:value': 22"
		, "  , 'eg:values': [42, 43]"
		, "  , 'eg:label': ['F', 'eff', {'@lang': 'cy', '@value': 'F'}, {'@lang': 'fr', '@value': 'f'}]"
		, "  }"
		, "]"
		);
    
    @Test public void testExpectAll() {    	
    	testQueryReturnsExpectedResults( "{}", allExpected );
    }
    
    @Test public void testExtractA() {    	
    	
    	String expectOnlyA = BunchLib.join
    		( "["
    		, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A-resource'}]"
    		, "  , 'eg:value': 17"
    		, "  , 'eg:values': [17, 18, 19]"
    		, "  , 'eg:label': [{'@lang': 'cy', '@value': 'A'}, 'A-one', 'A1']"
    		, "  }"
    		, "]"
    		);
    	
    	testQueryReturnsExpectedResults( "{'eg:value': {'@eq': 17}}", expectOnlyA );
    }
    
    @Test public void testExtractE() {    	
    	
    	String expectOnlyA = BunchLib.join
    		( "["
    		, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42, 99]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, "]"
    		);
    	
    	testQueryReturnsExpectedResults( "{'eg:value': {'@eq': 21}}", expectOnlyA );
    }
    
    @Test public void testExtractValues42() {    	
    	
    	String expectBDEandF = BunchLib.join
    		( "["
			, "  {"
			, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B'"
			, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B-resource'}]"
			, "  , 'eg:value': 18"
			, "  , 'eg:values': [42]"
			, "  , 'eg:label': ['B-one', 'B1']"
			, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F-resource'}]"
    		, "  , 'eg:value': 22"
    		, "  , 'eg:values': [42]"
    		, "  , 'eg:label': ['F', 'eff', {'@lang': 'cy', '@value': 'F'}, {'@lang': 'fr', '@value': 'f'}]"
    		, "  }"
    		, "]"
    		);
    	
    	testQueryReturnsExpectedResults( "{'eg:values': {'@eq': 42}}", expectBDEandF );
    }
    
    @Test public void testExtractValuesDEF_ByGE() {    	
    	
    	String expectDEF = BunchLib.join
    		( "["
			, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42, 99]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F-resource'}]"
    		, "  , 'eg:value': 22"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['F', 'eff', {'@lang': 'cy', '@value': 'F'}, {'@lang': 'fr', '@value': 'f'}]"
    		, "  }"
    		, "]"
    		);
    	
    	testQueryReturnsExpectedResults( "{'eg:value': {'@ge': 20}}", expectDEF );
    }    
    
    @Test public void testExtractABC_ByValueLt20() {
        String expectABC = BunchLib.join
    		( "["
    		, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A-resource'}]"
    		, "  , 'eg:value': 17"
    		, "  , 'eg:values': [17, 18, 19]"
    		, "  , 'eg:label': [{'@lang': 'cy', '@value': 'A'}, 'A-one', 'A1']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B-resource'}]"
    		, "  , 'eg:value': 18"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['B-one', 'B1']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C-resource'}]"
    		, "  , 'eg:value': 19"
    		, "  , 'eg:values': [99]"
    		, "  , 'eg:label': ['C-one', 'C1']"
    		, "  }"
    		, "]"
    		);
    	testQueryReturnsExpectedResults( "{'eg:value': {'@lt': 20}}", expectABC );
    }
    
    @Test public void testExtractAValueWithCombinedPredicates() {    	
    	String expectD = BunchLib.join
    		( "["
			, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, "]"
    		);
    	testQueryReturnsExpectedResults( "{'eg:value': {'@gt': 19, '@lt': 21}}", expectD );
    }
    
    @Test public void testExtractAValueWithANDedPredicates() {    	
    	String expectD = BunchLib.join
    		( "["
			, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, "]"
    		);
    	testQueryReturnsExpectedResults( "{'eg:value': {'@gt': 19}, '@and': [{'eg:value': {'@lt': 21}}]}", expectD);
    }
    
    @Test public void testSimpleOR() {    	
    	String expectDE = BunchLib.join
    		( "["
			, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42, 99]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, "]"
    		);
    	testQueryReturnsExpectedResults( "{'eg:value': {'@eq': 20}, '@or': [{'eg:value': {'@eq': 21}}]}", expectDE);
    }
    
    @Test public void testExtractNoValuesWithCombinedPredicates() {    	
    	String expectNone = "[]";
    	testQueryReturnsExpectedResults( "{'eg:value': {'@ge': 20, '@lt': 19}}", expectNone );
    }
    
    @Test public void testExtractNoValuesWithANDedCombinedPredicates() {    	
    	String expectNone = "[]";
    	testQueryReturnsExpectedResults( "{'eg:value': {'@ge': 20}, '@and': [{'eg:value': {'@lt': 19}}]}", expectNone );
    }
    
    @Test public void testExtractByTwoProperties() {
    	String expectD = BunchLib.join
        		( "["
				, "  {"
	    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
	    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
	    		, "  , 'eg:value': 20"
	    		, "  , 'eg:values': [43]"
	    		, "  , 'eg:label': ['D-two', 'D2']"
	    		, "  }"
	    		, "]"
	    		);
    	testQueryReturnsExpectedResults("{'eg:resource': {'@eq': {'@id': 'eg:DE-resource'}}, 'eg:values': {'@eq': 43}}", expectD);
    }
    
    @Test public void testNegatesSimpleFilter() {
    	String expectA = BunchLib.join
    		( "["
			, "  {"
			, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A'"
			, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A-resource'}]"
			, "  , 'eg:value': 17"
			, "  , 'eg:values': [17, 18, 19]"
			, "  , 'eg:label': [{'@lang': 'cy', '@value': 'A'}, 'A-one', 'A1']"
			, "  }"
    		, "]"
    		);
    	testQueryReturnsExpectedResults( "{'@not': [{'eg:value': {'@gt': 17}}]}", expectA );
    }
    
    @Test public void testNegatesTwoSimpleFilters() {
    	String expectC = BunchLib.join
    		( "["
    		, "  {"
			, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C'"
			, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C-resource'}]"
			, "  , 'eg:value': 19"
			, "  , 'eg:values': [99]"
			, "  , 'eg:label': ['C-one', 'C1']"
			, "  }"
    		, "]"
    		);
    	testQueryReturnsExpectedResults( "{'@not': [{'eg:value': {'@lt': 19}}, {'eg:value': {'@gt': 19}}]}", expectC );
    }
    
    @Test public void testNegatesOrOfTwoSimpleFilters() {
    	String expectC = BunchLib.join
    		( "["
    		, "  {"
			, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C'"
			, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C-resource'}]"
			, "  , 'eg:value': 19"
			, "  , 'eg:values': [99]"
			, "  , 'eg:label': ['C-one', 'C1']"
			, "  }"
    		, "]"
    		);
    	testQueryReturnsExpectedResults( "{'@not': [{'@or': [{'eg:value': {'@lt': 19}}, {'eg:value': {'@gt': 19}}]}]}", expectC );
    }
    
    @Test public void testNegatesAndOfTwoDifferentFilters_Not_Andxy() {
        String expectAE = BunchLib.join
    		( "["
    		, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/A-resource'}]"
    		, "  , 'eg:value': 17"
    		, "  , 'eg:values': [17, 18, 19]"
    		, "  , 'eg:label': [{'@lang': 'cy', '@value': 'A'}, 'A-one', 'A1']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C-resource'}]"
    		, "  , 'eg:value': 19"
    		, "  , 'eg:values': [99]"
    		, "  , 'eg:label': ['C-one', 'C1']"
    		, "  }"
    		, "]"
    		);        
         testQueryReturnsExpectedResults("{'@not': [{'@and': [{'eg:value': {'@ge': 18}}, {'eg:values': {'@eq': 42}}]}]}", expectAE);
    }
    
    @Test public void testNegatesAndOfTwoDifferentFilters_NotAndx() {
        String expectBCDEF = BunchLib.join( 
    		"["
    		, "  {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/B-resource'}]"
    		, "  , 'eg:value': 18"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['B-one', 'B1']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/C-resource'}]"
    		, "  , 'eg:value': 19"
    		, "  , 'eg:values': [99]"
    		, "  , 'eg:label': ['C-one', 'C1']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/D'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 20"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['D-two', 'D2']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/E'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/DE-resource'}]"
    		, "  , 'eg:value': 21"
    		, "  , 'eg:values': [42, 99]"
    		, "  , 'eg:label': ['E', 'e']"
    		, "  }"
    		, ", {"
    		, "  '@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F'"
    		, "  , 'eg:resource': [{'@id': 'http://www.epimorphics.com/test/dsapi/sprint3/search/F-resource'}]"
    		, "  , 'eg:value': 22"
    		, "  , 'eg:values': [42, 43]"
    		, "  , 'eg:label': ['F', 'eff', {'@lang': 'cy', '@value': 'F'}, {'@lang': 'fr', '@value': 'f'}]"
    		, "  }"
    		, "]"
    		);        
        testQueryReturnsExpectedResults("{'@not': [{'eg:values': {'@lt': 19}}]}", expectBCDEF);
    }
    
	public void testQueryReturnsExpectedResults(String queryString, String expectString) {
		QueryTestSupport.testQueryReturnsExpectedResults(man, queryString, expectString);
	}
	
	
}