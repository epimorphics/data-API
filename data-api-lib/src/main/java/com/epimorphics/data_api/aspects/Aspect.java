/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects;

import com.epimorphics.data_api.data_queries.Shortname;

public class Aspect {
	
	final String ID;
	final Shortname name;
	
	public Aspect(String ID, Shortname name) {
		this.ID = ID;
		this.name = name;
	}

	public String getID() {
		return ID;
	}

	public Shortname getName() {
		return name;
	}

	public String asVar() {
		return name.asVar();
	}

	public String asProperty() {
		return name.getCURIE();
	}

}
