/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.tests.TestAspects;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;

public class TestAPI_Dataset {

	@Test public void testNamedDataset() {
		assertEquals("bobcat", new API_Dataset( "bobcat" ).getName());
		assertEquals("feline", new API_Dataset( "feline" ).getName());
	}
	
	@Test public void testDatasetAspects() {
		API_Dataset d = new API_Dataset("example");
		Aspect a = new TestAspects.MockAspect("eh:/A");
		Aspect b = new TestAspects.MockAspect("eh:/B");
		assertEquals(0, d.getAspects().size());
	//
		d.add(a);
		assertEquals(BunchLib.set(a), d.getAspects());
	//
		d.add(b);
		assertEquals(BunchLib.set(a, b), d.getAspects());
	}
	
}
