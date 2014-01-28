/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.appbase.monitor.ConfigInstance;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.ResourceBasedConfig;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.rdf.model.Model;

import static com.epimorphics.data_api.config.JSONConstants.*;

public class API_Dataset extends ResourceBasedConfig implements ConfigInstance, JSONWritable {
	String name;
	
	final Set<Aspect> aspects = new HashSet<Aspect>();
	
	public API_Dataset(String name) {
		this.name = name;
	}
	
	public API_Dataset() {
	}
	
	public API_Dataset(Model config) {
	    // TODO implement - or do parsing externally
	    // TODO issue of when and how to retrieve any DSD from the source if not inline
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

    @Override
    public void writeTo(JSFullWriter out) {
        // TODO Auto-generated method stub
        
    }

    public void writeShortTo(JSFullWriter out) {
        // TODO Auto-generated method stub
        
    }

    public void writeShortTo(JSFullWriter out, String lang) {
        // TODO Auto-generated method stub
        
    }

}