/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects;

import static com.epimorphics.data_api.config.JSONConstants.*;

import java.util.Comparator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.data_api.config.JSONConstants;
import com.epimorphics.data_api.config.ResourceBasedConfig;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.Dsapi;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;

public class Aspect extends ResourceBasedConfig {
	
	static final Logger log = LoggerFactory.getLogger(Aspect.class);

	public static final Comparator<? super Aspect> compareAspects = new Comparator<Aspect>() {
		
		@Override public int compare(Aspect a, Aspect b) {
			if (a.getIsOptional() == b.getIsOptional()) return a.getID().compareTo(b.getID());
			return a.getIsOptional() ? +1 : -1;
		}
	};
	
	public static final Aspect NONE = null;
	
	/**
	 	Compare two aspects, taking into account whether they are optional or
	 	have some constraint on their value.
	 	
	 	[Constraint check suppressed, it causes mysterious test failures.
	 	TODO find out what's happening and sort it.
	 	
	 	Hmm, we believe it's to do with multi-valued properties. It's not
	 	clear /why/ reordering them breaks the tests. For the moment, if
	 	either property is multivalued, we shan't take constraints into
	 	account.
	 	]
	 	
	 	Non-optionals come before optionals. Constrained aspects come before
	 	non-constrained aspects. Otherwise, they are ordered by their IDs spelling.
	 	
	 	[Tricker than that. May have to disable some optimisations until
	 	the framework can cope with them.]
	*/
	public static final Comparator<? super Aspect> compareConstrainedAspects
		(final Shortname searchProperty, final Set<Aspect> constrained) {
		return new Comparator<Aspect>() {		
			
			@Override public int compare(Aspect a, Aspect b) {
				
				if (searchProperty != null) {
					if (a.name.equals(searchProperty)) return -1;
					if (b.name.equals(searchProperty)) return +1;
				}
				
				boolean aIsConstrained = constrained.contains(a) && !a.getIsMultiValued();
				boolean bIsConstrained = constrained.contains(b) && !b.getIsMultiValued();
				if (aIsConstrained != bIsConstrained) return aIsConstrained ? -1 : +1;
			
				boolean aIsOptional = a.getIsOptional();
				if (aIsOptional != b.getIsOptional()) return aIsOptional ? +1 : -1;
				
				return a.getID().compareTo(b.getID());
			}
		};
	}
	
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
	
	/**
		Initialise this Aspect with the given shortName 'spoo:local',
		with its full name being given using the prefixes pm
		to expand the prefix 'spoo'. The prefixes are retained
		and may be extracted using getPrefixes().
	*/
	public Aspect(PrefixMapping pm, String shortName) {
		String fullName = pm.expandPrefix(shortName);		
		this.ID = fullName;
		this.name = new Shortname(pm, shortName);
		this.explictPrefixes = pm;
	}
	
	public boolean equals(Object other) {
		return other instanceof Aspect && same( (Aspect) other );
	}
	
	private boolean same(Aspect other) {
		return this.ID.equals(other.ID);
	}

	@Override public PrefixMapping getPrefixes() {
		return explictPrefixes == null ? super.getPrefixes() : explictPrefixes;
	}

	public Aspect(Resource aspectRoot) {
	    super(aspectRoot);
	    ID = aspectRoot.getURI();
	    if (ID == null) {
	        Resource prop = aspectRoot.getPropertyResourceValue(Dsapi.property);
	        if (prop != null) {
	            ID = prop.getURI();
	        } else {
	            // TODO The ID should not be tied to the aspect definition but needs more refactoring here to sort it out
	            throw new EpiException("Failed to parse aspect specification - aspects need to be either URI resources or have a dssapi:property whose value is a URI resource");
	        }
	    }
	    PrefixMapping pm = getPrefixes();
	    name = new Shortname(pm, pm.shortForm(ID));
	    configureFrom(aspectRoot);
	}
	
	public void configureFrom(Resource aspectRoot) {
		setIsOptional( RDFUtil.getBooleanValue(aspectRoot, Dsapi.optional, false));
		setRangeType(aspectRoot.getPropertyResourceValue(RDFS.range));
        setIsMultiValued( RDFUtil.getBooleanValue(aspectRoot, Dsapi.multivalued, false) );
        setPropertyPath( RDFUtil.getStringValue(aspectRoot, Dsapi.propertyPath));
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
		if (type != null) {
			log.debug("set rangeType of " + name.getCURIE() + " to " + type);
			this.rangeType = type;
		}
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
