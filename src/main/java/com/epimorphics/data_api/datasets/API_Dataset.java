/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.data_api.aspects.Aspect;

public class API_Dataset {

	String name;
	
	final Set<Aspect> aspects = new HashSet<Aspect>();
	
	public API_Dataset(String name) {
		System.err.println( ">> creating an API dataset named " + name + "." );
		this.name = name;
	}
	
	public API_Dataset() {
		System.err.println( ">> creating an anonymous API dataset." );
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Set<Aspect> getAspects() {
		return aspects;
	}

	public void add(Aspect a) {
		aspects.add(a);
	}
	
}