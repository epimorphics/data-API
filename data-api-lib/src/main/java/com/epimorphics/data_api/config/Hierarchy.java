/******************************************************************
 * File:        Hierachy.java
 * Created by:  Dave Reynolds
 * Created on:  14 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config;

import java.util.List;

import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.util.EpiException;
import com.epimorphics.vocabs.Cube;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Holds specification of a hierarchy (associated with a Dataset). Allows
 * for skos:ConceptScheme, skos:Collection and qb:HierarchicalCodeList.
 */
public class Hierarchy extends ResourceBasedConfig {
    static final String VAR_TEMPLATE =  "%VAR%";
    static final String ARG_TEMPLATE =  "%ARG%";
    
    List<Resource> roots;  // Explicitly enumerated top level roots if relevant
    Resource type;
    
    String memberQueryTemplate; 
    String topRootsQueryTemplate; 
    String childQueryFragment; 
    
    boolean needsDistinct = false;
    
    public Hierarchy(Resource codelist) {
        super(codelist);
        if (codelist.hasProperty(RDF.type, SKOS.ConceptScheme)) {
            type = SKOS.ConceptScheme;
            memberQueryTemplate = "?%VAR% skos:inScheme <" + codelist.getURI() + "> .";
            topRootsQueryTemplate = "<" + codelist.getURI() + "> skos:hasTopConcept|^skos:topConceptOf ?%VAR% .\n";
            childQueryFragment = "skos:narrower|^skos:broader";
            needsDistinct = true;
            
        } else if (codelist.hasProperty(RDF.type, SKOS.Collection)) {
            type = SKOS.Collection;
            memberQueryTemplate = "<" + codelist.getURI() + "> skos:member ?%VAR% .\n";
            topRootsQueryTemplate = memberQueryTemplate;
            childQueryFragment = "<http://example.com/noSuchProperty>";
            
        } else if (codelist.hasProperty(RDF.type, Cube.HierarchicalCodeList)) {
            type = Cube.HierarchicalCodeList;
            
            roots = RDFUtil.getResourceValues(codelist, Cube.hierarchyRoot);
            
            StringBuffer buff = new StringBuffer();
            buff.append("VALUES ?%VAR% {");
            for (Resource r : roots) {
                buff.append("<" + r.getURI() + "> ");
            }
            buff.append("}\n");
            topRootsQueryTemplate = buff.toString();
            
            String var = safeVarName();
            String pcf = parentChildFragment(codelist);
            memberQueryTemplate = topRootsQueryTemplate.replace(VAR_TEMPLATE, var) + "?" + var + " " + pcf + "* ?%VAR% .\n";
            
            childQueryFragment =  pcf;
        } else {
            throw new EpiException("Type of code lists not recognized, only skos:ConceptScheme, skos:Collection and qb:HierarchicalCodeList supported. " + codelist);
        }
    }
    
    private String parentChildFragment(Resource codelist) {
        boolean inverse = false;
        Resource parentChildProp = codelist.getPropertyResourceValue(Cube.parentChildProperty);
        if (parentChildProp != null && parentChildProp.isAnon()) {
            parentChildProp = parentChildProp.getPropertyResourceValue(OWL.inverseOf);
            inverse = true;
        }
        if (parentChildProp == null || parentChildProp.isAnon()) {
            // TODO better configuration error reporting
            throw new EpiException("qb:HierarchicalCodeList without a legal parent-child property: " + codelist);
        }
        return (inverse ? "^" : "") + "<" + parentChildProp.getURI() + ">";
    }
    
    private String safeVarName() {
        return "___1" ;  // Safe only by assuming current format for queries, may need to gensym
    }

    public Resource getType() {
        return type;
    }

    /**
     * Return a query pattern which binds a var with the given name to members of the code list
     */
    public String getMemberQuery(String varname) {
        return memberQueryTemplate.replace(VAR_TEMPLATE, varname);
    }

    /**
     * Return a query pattern which binds a var with the given name to the top level roots of the code list
     */
    public String getTopRootsQuery(String varname) {
        return topRootsQueryTemplate.replace(VAR_TEMPLATE, varname);
    }

    /**
     * Return a query fragment, a property path expression, which links a parent node to a child node in the code list.
     * Returns null for code lists with no parent/child relationship.
     */
    public String getChildQueryFragment() {
        return childQueryFragment;
    }
    
    /**
     * Return true if the hierarchy query follows multiple paths
     * and so needs a DISTINCT qualifier in the query
     */
    public boolean getNeedsDistinct() {
        return needsDistinct;
    }

    // TODO JSON serialization
}
