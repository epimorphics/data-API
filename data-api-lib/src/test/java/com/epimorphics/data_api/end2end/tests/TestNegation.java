/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.end2end.tests;

import org.junit.Test;

import com.epimorphics.data_api.libs.BunchLib;

public class TestNegation extends Setup {

	static final String expectABDEF = BunchLib.join
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
	
	static final String expectAllButA = BunchLib.join
		( "["
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

	@Test public void testNegatesSimpleFilter() {
    	testQueryReturnsExpectedResults( "{'@not': [{'eg:value': {'@gt': 17}}]}", expectA );
    }
    
    @Test public void testNegatesTwoSimpleFilters() {
    	testQueryReturnsExpectedResults( "{'@not': [{'eg:value': {'@lt': 19}}, {'eg:value': {'@gt': 19}}]}", expectC );
    }
    
    @Test public void testNegatesOrOfTwoSimpleFilters() {
    	testQueryReturnsExpectedResults( "{'@not': [{'@or': [{'eg:value': {'@lt': 19}}, {'eg:value': {'@gt': 19}}]}]}", expectC );
    }
    
    @Test public void testNegateSearch() {
    	testQueryReturnsExpectedResults("{'@search': 'notfound'}}", "[]" );
    	testQueryReturnsExpectedResults("{'@not': [{'@search': 'notfound'}]}", allExpected );
    }
    
    @Test public void testOptionalAspect() {
    	testQueryReturnsExpectedResults("{'eg:resource': {'@eq': {'@id': 'eg:C-resource'}}}", expectC);
    }

    @Test public void testNegatedOptionalAspect() {
    	testQueryReturnsExpectedResults("{'@not': [{'eg:resource': {'@eq': {'@id': 'eg:C-resource'}}}]}", expectABDEF);
    }
    
    @Test public void testNegateNegatedOptionalAspect() {
    	testQueryReturnsExpectedResults("{'@not': [{'@not': [{'eg:resource': {'@eq': {'@id': 'eg:C-resource'}}}]}]}", expectC);
    }
    
    @Test public void testNegateBelow_Unnegated() {
    	testQueryReturnsExpectedResults("{'eg:resource': {'@below': {'@id': 'eg:poggles'}}}", expectA);
    }
    
    @Test public void testNegateBelow() {
    	testQueryReturnsExpectedResults("{'@not': [{'eg:resource': {'@below': {'@id': 'eg:poggles'}}}]}", expectAllButA);
    }
    
    @Test public void testNegateBelow_TwiceNegated() {
    	testQueryReturnsExpectedResults("{'@not': [{'@not': [{'eg:resource': {'@below': {'@id': 'eg:poggles'}}}]}]}", expectA);
    }
    
    @Test public void testNegateNotFilter() {    	
    	String expectAB = BunchLib.join
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
			, "]"
			);
    	
    	testQueryReturnsExpectedResults( "{'@not': [{'@not': [{'eg:value': {'@lt': 19}}]}]}", expectAB );
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
    
}
