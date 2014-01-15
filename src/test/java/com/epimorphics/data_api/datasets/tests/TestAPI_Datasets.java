/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets.tests;

import static org.junit.Assert.*;


import org.junit.Test;

import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.datasets.API_Datasets;
import com.epimorphics.data_api.libs.BunchLib;

public class TestAPI_Datasets {

	@Test public void testDatasetContainer() {
		API_Dataset d = new API_Dataset("nameHere");
		API_Datasets s = new API_Datasets();
		assertEquals(0, s.getDatasets().size());
	//
		s.add(d);
		assertEquals(1, s.getDatasets().size());
		assertEquals(BunchLib.set(d), s.getDatasets());
	}
}
