/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.end2end.tests;

import org.junit.Test;

import com.epimorphics.data_api.libs.BunchLib;

public class TestAnd extends Setup {
    
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
    
    @Test public void testExtractNoValuesWithANDedCombinedPredicates() {    	
    	String expectNone = "[]";
    	testQueryReturnsExpectedResults( "{'eg:value': {'@ge': 20}, '@and': [{'eg:value': {'@lt': 19}}]}", expectNone );
    }

}
