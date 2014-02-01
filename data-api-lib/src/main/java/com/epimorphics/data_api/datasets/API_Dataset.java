/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets;

import static com.epimorphics.data_api.config.JSONConstants.DATA_API;
import static com.epimorphics.data_api.config.JSONConstants.DESCRIPTION;
import static com.epimorphics.data_api.config.JSONConstants.DEV_API;
import static com.epimorphics.data_api.config.JSONConstants.ID;
import static com.epimorphics.data_api.config.JSONConstants.LABEL;
import static com.epimorphics.data_api.config.JSONConstants.STRUCTURE_API;
import static com.epimorphics.data_api.config.JSONConstants.URI;
import static com.epimorphics.data_api.config.JSONConstants.ASPECTS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.monitor.ConfigInstance;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.config.ResourceBasedConfig;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.vocabs.Cube;
import com.epimorphics.vocabs.Dsapi;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Resource;

public class API_Dataset extends ResourceBasedConfig implements ConfigInstance {
    static Logger log = LoggerFactory.getLogger(API_Dataset.class);
    
    String name;
	String query;
	DSAPIManager manager;
	
	final Set<Aspect> aspects = new HashSet<Aspect>();
	
	public API_Dataset(String name) {
		this.name = name;
	}
	
	public API_Dataset() {
	}
	
	public API_Dataset(Resource config, DSAPIManager manager) {
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

    /**
     * Full json serialization used to report the dataset structure
     */
    public JSONWritable asJson() {
        return asJson(null);
    }
    
    /**
     * Full json serialization used to report the dataset structure, language specific
     */
    public JSONWritable asJson(String lang) {
        return new Writer(lang);
    }

    /**
     * Short form json serialization using the dataset list endpoint, language specific
     */
    public void writeShortTo(JSFullWriter out, String lang) {
        out.startObject();
        writeSummary(out, lang);
        out.finishObject();
    }
    
    private void writeSummary(JSFullWriter out, String lang) {
        out.pair(ID, getName());
        out.pair(URI, root.getURI());
        out.pair(LABEL, getLabel(lang));
        out.pair(DESCRIPTION, getDescription(lang));
        if (manager != null) {
            String base = manager.getApiBase() + "/dataset/" + name;
            out.pair(DATA_API, base + "/data");
            out.pair(STRUCTURE_API, base + "/structure");
            out.pair(DEV_API, base + "/dev");
        }
    }

    public void writeJson(JSFullWriter out, String lang) {
        out.startObject();
        writeSummary(out, lang);
        out.key(ASPECTS);
        out.startArray();
        for (Iterator<Aspect> ai = aspects.iterator(); ai.hasNext();) {
            ai.next().writeJson(out, lang);
            if (ai.hasNext()) {
                out.arraySep();
            }
        }
        out.finishArray();
        out.finishObject();
    }
    
    class Writer implements JSONWritable {
        String lang;
        public Writer(String lang) {  this.lang = lang;  }
        
        @Override
        public void writeTo(JSFullWriter out) {
            writeJson(out, lang);
        }
    }

}