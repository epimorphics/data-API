/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.aspects;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.data_queries.Shortname;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;

public class Aspect {
	
	final String ID;
	final Shortname name;
	
	List<Node> labels = new ArrayList<Node>();
	List<Node> descriptions = new ArrayList<Node>();
	
	boolean isMultiValued = false;
	boolean isOptional = false;
	
	Resource rangeType = null;
	
	public Aspect(String ID, Shortname name) {
		this.ID = ID;
		this.name = name;
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

	public String asVar() {
		return name.asVar();
	}

	public String asProperty() {
		return name.getCURIE();
	}

	public List<Node> getLabels() {
		return labels;
	}

	public Aspect setLabels(List<Node> labels) {
		this.labels = labels;
		return this;
	}

	public List<Node> getDescriptions() {
		return descriptions;
	}

	public Aspect setDescriptions(List<Node> descriptions) {
		this.descriptions = descriptions;
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
