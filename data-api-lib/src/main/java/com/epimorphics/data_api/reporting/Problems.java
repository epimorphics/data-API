/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.reporting;

import java.util.ArrayList;
import java.util.List;

public class Problems {

	final List<Problem> problems = new ArrayList<Problem>();
	
	public Problems() {
	}
	
	public int size() {
		return problems.size();
	}
	
	public boolean isOK() {
		return problems.isEmpty();
	}

	public Problems add(String message) {
		problems.add(new Problem(message));
		return this;
	}

	public List<Problem> getProblems() {
		return problems;
	}

	public String getProblemStrings() {
		StringBuilder sb = new StringBuilder();
		for (Problem p: problems) sb.append(p.message).append("\n");
		return sb.toString();
	}
}