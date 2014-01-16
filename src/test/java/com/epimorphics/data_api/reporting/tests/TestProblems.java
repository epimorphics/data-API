/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.reporting.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.reporting.Problem;
import com.epimorphics.data_api.reporting.Problems;

public class TestProblems {
	
	@Test public void testEmptyProblems() {
		assertEquals(0, new Problems().size());
	}
	
	@Test public void testSingleProblems() {
		assertEquals(1, new Problems().add("oops").size());
	}
	
	@Test public void testSingleProblemText() {
		List<Problem> p = new Problems().add("oops").getProblems();
		assertEquals(1, p.size());
		assertEquals("oops", p.get(0).toText());
	}
}
