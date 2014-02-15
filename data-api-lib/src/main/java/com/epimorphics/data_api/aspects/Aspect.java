/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects;

import static com.epimorphics.data_api.config.JSONConstants.*;

import com.epimorphics.data_api.config.DefaultPrefixes;
import com.epimorphics.data_api.config.JSONConstants;
import com.epimorphics.data_api.config.ResourceBasedConfig;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.Dsapi;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Aspect extends ResourceBasedConfig {
	String ID;
	final Shortname name;
	String rangeDataset;
	
	boolean isMultiValued = false;
	boolean isOptional = false;
	
	Resource rangeType = null;
	Shortname belowPredicate = new Shortname(DefaultPrefixes.get(), "skos:broader" );
	
	public Aspect(String ID, Shortname name) {
		this.ID = ID;
		this.name = name;
	}
	
	public Aspect(Resource aspect) {
	    super(aspect);
	    ID = aspect.getURI();
	    if (ID == null) {
	        Resource prop = aspect.getPropertyResourceValue(Dsapi.property);
	        if (prop != null) {
	            ID = prop.getURI();
	        } else {
	            // TODO The ID should not be tied to the aspect definition but needs more refactoring here to sort it out
	            throw new EpiException("Internal error - can't handle bNode aspects properly yet");
	        }
	    }
	    PrefixMapping pm = getPrefixes();
	    name = new Shortname(pm, pm.shortForm(ID));
	    
	    rangeType = aspect.getPropertyResourceValue(RDFS.range);
	}
	
	@Override public String toString() {
		return name.toString();
	}

	public String getID() {
		return ID;
	}

	public Shortname getName() {
		return name;
	}
    
    // From base class have:
    // getLabel()       getLabel(lang)
    // getDescription() getDescription(lang)

	public String asVar() {
		return name.asVar();
	}

	public String asProperty() {
		return name.getCURIE();
	}
	
	public Shortname getBelowPredicate() {
		return belowPredicate;
	}
	
	public Aspect setBelowPredicate(Shortname belowPredicate) {
		this.belowPredicate = belowPredicate;
		return this;
	}

	public boolean getIsMultiValued() {
		return isMultiValued;
	}

	public Aspect setIsMultiValued(boolean isMultiValued) {
		this.isMultiValued = isMultiValued;
		return this;
	}

	public boolean getIsOptional() {
		return isOptional;
	}

	public Aspect setIsOptional(boolean isOptional) {
		this.isOptional = isOptional;
		return this;
	}

	public Aspect setRangeType(Resource type) {
		this.rangeType = type;
		return this;
	}
	
	public Resource getRangeType() {
		return rangeType;
	}
    
	
    public String getRangeDataset() {
        return rangeDataset;
    }

    public void setRangeDataset(String codelist) {
        this.rangeDataset = codelist;
    }

    /**
     * Full json serialization used to report the dataset structure, language specific
     */
    public JSONWritable asJson(String lang) {
        return new Writer(lang);
    }

    public void writeJson(JSFullWriter out, String lang) {
        out.startObject();
        out.pair(NAME, name.getCURIE());
        out.pair(JSONConstants.ID, ID);
        safeOut(out, LABEL, getLabel(lang));
        safeOut(out, DESCRIPTION, getDescription(lang));
        out.pair(IS_OPTIONAL, isOptional);
        out.pair(IS_MULTIVALUED, isMultiValued);
        safeOut(out, RANGE_TYPE, rangeType == null ? null : rangeType.getURI());
        safeOut(out, RANGE_DATASET, rangeDataset);
        out.finishObject();
    }
    
    private void safeOut(JSFullWriter out, String key, String value) {
        if (value != null) {
            out.pair(key, value);
        }
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
