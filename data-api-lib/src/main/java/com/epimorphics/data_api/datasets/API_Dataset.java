/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.monitor.ConfigInstance;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.ResourceBasedConfig;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.vocabs.Cube;
import com.epimorphics.vocabs.Dsapi;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Resource;

public class API_Dataset extends ResourceBasedConfig implements ConfigInstance, JSONWritable {
    static Logger log = LoggerFactory.getLogger(API_Dataset.class);
    
    String name;
	String query;
	
	final Set<Aspect> aspects = new HashSet<Aspect>();
	
	public API_Dataset(String name) {
		this.name = name;
	}
	
	public API_Dataset() {
	}
	
	public API_Dataset(Resource config) {
	    super(config);
	    configureBaseQuery();
	    configureName();
	}
	
    private void configureBaseQuery() {
        query = getStringValue(Dsapi.baseQuery, null);
        if (query == null) {
            Resource dataset = getResourceValue(Dsapi.qb_dataset);
            if (dataset != null) {
                query = "?item  <" + Cube.dataSet + "> <" + getResourceValue(Dsapi.qb_dataset).getURI() + "> .";
            } else {
                query = "";
            }
        }
    }
    
    private void configureName() {
        name = RDFUtil.getStringValue(root, SKOS.notation, RDFUtil.getLocalname(root));
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
	
	// From base class have:
	// getLabel()       getLabel(lang)
	// getDescription() getDescription(lang)
	
	/**
	 * Return the SPARQL pattern which finds entries in the data set and binds them to ?item.
	 */
	public String getBaseQuery() {
	    return query;
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