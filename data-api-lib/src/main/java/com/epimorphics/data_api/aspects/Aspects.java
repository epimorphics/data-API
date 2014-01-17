/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.aspects;

import java.util.HashSet;
import java.util.Set;

/**
    An Aspects holds multiple Aspect's.
*/
public class Aspects {

	final Set<Aspect> aspects = new HashSet<Aspect>();
	
	public Aspects() {		
	}
	
	public Set<Aspect> getAspects() {
		return aspects;
	}

	public Aspects include(Aspect a) {
		aspects.add(a);
		return this;
	}

}
