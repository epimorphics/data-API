/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects;

import static com.epimorphics.data_api.config.JSONConstants.*;

import java.util.Comparator;

import com.epimorphics.data_api.config.JSONConstants;
import com.epimorphics.data_api.config.ResourceBasedConfig;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.Dsapi;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Aspect extends ResourceBasedConfig {
	
	public static final Comparator<? super Aspect> compareAspects = new Comparator<Aspect>() {
		
		@Override public int compare(Aspect a, Aspect b) {
			if (a.getIsOptional() == b.getIsOptional()) return a.getID().compareTo(b.getID());
			return a.getIsOptional() ? +1 : -1;
		}
	};
	
	String ID;
	final Shortname name;
	String rangeDataset;
	
	boolean isMultiValued = false;
	boolean isOptional = false;
	
	Resource rangeType = null;
	String belowPredicate = null;
	
	String propertyPath = null;
	
	static final String DefaultBelowPredicate = "skos:narrower";
	
	PrefixMapping explictPrefixes = null;
	
	public Aspect(PrefixMapping pm, String shortName) {
		String fullName = pm.expandPrefix(shortName);		
		this.ID = fullName;
		this.name = new Shortname(pm, shortName);
		this.explictPrefixes = pm;
	}
	
	@Override public PrefixMapping getPrefixes() {
		return explictPrefixes == null ? super.getPrefixes() : explictPrefixes;
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
	            throw new EpiException("Failed to parse aspect specification - aspects need to be either URI resources or have a dssapi:property whose value is a URI resource");
	        }
	    }
	    PrefixMapping pm = getPrefixes();
	    name = new Shortname(pm, pm.shortForm(ID));
	    rangeType = aspect.getPropertyResourceValue(RDFS.range);
	}
	
	@Override public int hashCode() {
		return name.hashCode();
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

	/**
	    The variable-name rendering of this aspect, with no
	    "?" prefix.
	*/
	public String asVarName() {
		return name.getVarName();
	}
	
	/**
	    The SPARQL variable rendering of this aspect, with
	    the "?" prefix.
	*/
	public String asVar() {
		return "?" + name.getVarName();
	}

	public String asProperty() {
		return propertyPath == null ? name.getCURIE() : propertyPath;	
	}
	
	public String getBelowPredicate(API_Dataset dataset) {
	    if (belowPredicate == null) {
	        if (rangeDataset != null) {
	            API_Dataset rangeDS = dataset.getManager().getDataset(rangeDataset);
	            if (rangeDS.isHierarchy()) {
	                belowPredicate = rangeDS.getHierarchy().getChildQueryFragment();
	            }
	        }
	    }
        if (belowPredicate == null) {
            belowPredicate = DefaultBelowPredicate;
        }
		return belowPredicate;
	}
	
	public Aspect setBelowPredicate(String belowPredicate) {
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
    
    public String getPropertyPathRaw() {
    	return propertyPath;
    }
    
    public String getPropertyPath() {
    	return asProperty();
    }
    
    public Aspect setPropertyPath(String path) {
    	checkLegalPropertyPath(path);
    	propertyPath = path;
    	return this;
    }

    private void checkLegalPropertyPath(String path) {
    	/*
    	 	TODO.
    		Place-holder for checking that a property path is
    		legal, ie has SPARQL syntax. We may throw it away
    		instead.
    	*/
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
