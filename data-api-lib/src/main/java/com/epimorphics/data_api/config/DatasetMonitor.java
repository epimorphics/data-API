/******************************************************************
 * File:        DatasetMonitor.java
 * Created by:  Dave Reynolds
 * Created on:  28 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.monitor.ConfigMonitor;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.rdfutil.QueryUtil;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.vocabs.Cube;
import com.epimorphics.vocabs.Dsapi;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Monitor a directory of configuration files, each of which species
 * an API Dataset to be made available.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DatasetMonitor extends ConfigMonitor<API_Dataset>{
    static Logger log = LoggerFactory.getLogger(DatasetMonitor.class);

    protected DSAPIManager manager;
    
    public void setManager(DSAPIManager manager) {
        this.manager = manager;
    }

    @Override
    protected API_Dataset configure(File file) {
        Model config = FileManager.get().loadModel( file.getPath() );
        Resource configRoot = RDFUtil.findRoot(config);
        
        // Set the default global prefixes
        // stops applications rebinding them in a way with might break internal assumptions
        config.setNsPrefixes(DefaultPrefixes.get());
        
        // The config may reference dataset or DSD information in the source
        // If so pull in a local copy
        Resource dataset = RDFUtil.getResourceValue(configRoot, Dsapi.qb_dataset);
        Resource dsd = RDFUtil.getResourceValue(configRoot, Dsapi.qb_dsd);
        if (dataset != null && !hasProperties(dataset)) {
            addClosure(dataset);
            if (dsd == null) {
                dsd = RDFUtil.getResourceValue(dataset, Cube.structure);
            }
            if (dsd != null && !hasProperties(dsd)) {
                addClosure(dsd);
                if (dsd.isURIResource()) {
                    // Try to fetch the descriptions of the components
                    String componentQuery = String.format("PREFIX qb: <%s> DESCRIBE ?x WHERE {<%s> qb:component / (qb:dimension | qb:attribute | qb:measure | qb:componentProperty) ?x}",
                            Cube.getURI(), dsd.getURI());
                    addClosure(dataset, manager.getSource().describe(componentQuery));
                }
            }
        }
        
        // Also pull in property definitions for any aspect property declared inline
        List<Resource> aspectprops = QueryUtil.connectedResources(configRoot, "dsapi:aspect / dsapi:property");
        if ( ! aspectprops.isEmpty() ) {
            String[] uris = new String[ aspectprops.size() ];
            for (int i = 0; i < aspectprops.size(); i++) uris[i] = aspectprops.get(i).getURI();
            config.add( ModelFactory.createModelForGraph( manager.getSource().describeAll(uris)) );
        }
        
        API_Dataset dsapi = new API_Dataset(configRoot, manager); 
        if (dsd != null) {
            parseDSD(dsapi, dsd);
        } else {
            parseAspects(dsapi, configRoot);
        }
        return dsapi;
    }
    
    private void parseDSD(API_Dataset dsapi, Resource dsd) {
        for (Resource component : RDFUtil.allResourceValues(dsd, Cube.component)) {
            if (component.hasProperty(Cube.dimension)) {
                addAspect(dsapi, RDFUtil.getResourceValue(component, Cube.dimension), true);
            } else if (component.hasProperty(Cube.measure)) {
                addAspect(dsapi, RDFUtil.getResourceValue(component, Cube.measure), true);
            } else if (component.hasProperty(Cube.attribute)) {
                boolean required = RDFUtil.getBooleanValue(component, Cube.componentRequired, false);
                addAspect(dsapi, RDFUtil.getResourceValue(component, Cube.attribute), required);
            } else {
                log.warn("Failed to parse on of the components of dsd " + dsd + ", component was " + component);
            }
        }
    }
    
    private void addAspect(API_Dataset dsapi, Resource aspect, boolean required) {
        Aspect a = new Aspect(aspect);
        a.setIsOptional(!required);
        dsapi.add(a);
    }
    
    private static Property[] mergeProps = new Property[]{ RDFS.label, SKOS.prefLabel, RDFS.comment, RDFS.range, DCTerms.description};
    
    private void parseAspects(API_Dataset dsapi, Resource root) {
        for (Resource aspect : RDFUtil.allResourceValues(root, Dsapi.aspect)) {
            Resource decl = RDFUtil.getResourceValue(aspect, Dsapi.property);
            if (decl != null) {
                for (Property p : mergeProps) {
                    mergeProp(aspect, decl, p);
                }
            }
            Aspect a = new Aspect(aspect);
            a.setIsMultiValued( RDFUtil.getBooleanValue(aspect, Dsapi.multivalued, false) );
            a.setIsOptional( RDFUtil.getBooleanValue(aspect, Dsapi.optional, false) );
            // TODO parse property paths
            // TODO parse range constraints
            dsapi.add(a);
        }
    }
    
    private boolean hasProperties(Resource r) {
        StmtIterator si = r.listProperties();
        boolean result = si.hasNext();
        si.close();
        return result;
    }
    
    private void addClosure(Resource root) {
        if (root.isURIResource()) {
            Graph closure = manager.getSource().describeAll( root.getURI() );
            addClosure(root, closure);
        }
    }
    
    private void addClosure(Resource root, Graph closure) {
        root.getModel().add( ModelFactory.createModelForGraph(closure) );
    }

    private void mergeProp(Resource aspect, Resource decl, Property prop) {
        if ( ! aspect.hasProperty(prop) ) {
            for (StmtIterator i = decl.listProperties(prop); i.hasNext();) {
                Statement s = i.next();
                aspect.addProperty(prop, s.getObject());
            }
        }
    }
}
