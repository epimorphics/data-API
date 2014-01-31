/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects;

import com.epimorphics.data_api.config.ResourceBasedConfig;
import com.epimorphics.data_api.data_queries.Shortname;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

public class Aspect extends ResourceBasedConfig {
	
	final PrefixMapping pm = PrefixMapping.Factory.create()
		.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core")
		;
	
	final String ID;
	final Shortname name;
	
	boolean isMultiValued = false;
	boolean isOptional = false;
	
	Resource rangeType = null;
	Shortname belowPredicate = new Shortname(pm, "skos:broader" );
	
	public Aspect(String ID, Shortname name) {
		this.ID = ID;
		this.name = name;
	}
	
	public Aspect(Resource aspect) {
	    super(aspect);
	    ID = aspect.getURI();
	    PrefixMapping pm = getPrefixes();
	    name = new Shortname(pm, pm.shortForm(ID));
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

}
