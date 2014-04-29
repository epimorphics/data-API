/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.end2end.tests;

import org.junit.Test;

import com.epimorphics.data_api.libs.BunchLib;

public class TestOr extends Setup {
    
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

}
