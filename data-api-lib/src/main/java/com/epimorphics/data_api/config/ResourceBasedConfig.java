/******************************************************************
 * File:        ResourceBasedConfig.java
 * Created by:  Dave Reynolds
 * Created on:  28 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config;

import com.epimorphics.rdfutil.RDFUtil;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Utility or base class for objects which have been configured via an
 * RDF source file.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class ResourceBasedConfig {
    protected Model config;
    protected Resource root;
    
    public ResourceBasedConfig(Model config) {
        this.config = config;
        this.root = RDFUtil.findRoot(config);
    }
    
    /**
     * Empty constructor used by extending classes that have a non-RDF test mode
     */
    public ResourceBasedConfig() {
    }

    /**
     * Return a string-valued configuration property
     */
    public String getStringValue(Property p, String defaultValue) {
        return RDFUtil.getStringValue(root, p, defaultValue);
    }

    /**
     * Return a resource-valued configuration property
     */
    public Resource getResourceValue(Property p) {
        return RDFUtil.getResourceValue(root, p);
    }

    /**
     * Return a boolean-valued configuration property
     */
    public boolean getBooleanValue(Property p, boolean defaultValue) {
        return RDFUtil.getBooleanValue(root, p, defaultValue);
    }
    
    /**
     * The root configuration resource from which values can be obtained
     */
    public Resource getRoot() {
        return root;
    }
    
    /**
     * Return a suitable label for the resource, not language specific
     */
    public String getLabel() {
        return RDFUtil.getLabel(root);
    }
    
    /**
     * Return a suitable label for the resource, preferably in the given language
     */
    public String getLabel(String lang) {
        return RDFUtil.getLabel(root, lang);
    }
    
    
    /**
     * Return a suitable description for the resource, not language specific
     */
    public String getDescription() {
        return RDFUtil.getDescription(root);
    }
    
    /**
     * Return a suitable description for the resource, preferably in the given language
     */
    public String getDescription(String lang) {
        return RDFUtil.findLangMatchValue(root, lang, RDFUtil.descriptionProps);
    }

}
