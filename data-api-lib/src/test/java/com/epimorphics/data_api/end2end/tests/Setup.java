/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.end2end.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Before;

import com.epimorphics.appbase.core.App;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.libs.BunchLib;

public class Setup {

	App testapp;
    DSAPIManager man;
    
	static final String allExpected = BunchLib.join
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
    
    @Before public void startup() throws IOException  {
		testapp = new App("testapp", new File("src/test/data/query-testing/test.conf"));
        man = testapp.getComponentAs("dsapi", DSAPIManager.class);
    }
    
	public void testQueryReturnsExpectedResults(String queryString, String expectString) {
		QueryTestSupport.testQueryReturnsExpectedResults(man, queryString, expectString);
	}
}

