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

import com.epimorphics.appbase.monitor.ConfigMonitor;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.vocabs.Cube;
import com.epimorphics.vocabs.Dsapi;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;

public class DatasetMonitor extends ConfigMonitor<API_Dataset>{
    protected DSAPIManager manager;
    
    public void setManager(DSAPIManager manager) {
        this.manager = manager;
    }

    @Override
    protected API_Dataset configure(File file) {
        Model config = FileManager.get().loadModel( file.getPath() );
        
        // The config may reference dataset or DSD information in the source
        // If so pull in a local copy
        Resource configRoot = RDFUtil.findRoot(config);
        
        Resource dataset = RDFUtil.getResourceValue(configRoot, Dsapi.qb_dataset);
        Resource dsd = RDFUtil.getResourceValue(configRoot, Dsapi.qb_dsd);
        if (dataset != null && !hasProperties(dataset)) {
            addClosure(dataset);
            if (dsd == null) {
                dsd = RDFUtil.getResourceValue(dataset, Cube.structure);
            }
            if (dsd != null && !hasProperties(dsd)) {
                addClosure(dsd);
                // TODO  Scan for component properties and pull those in
            }
        }
        return new API_Dataset(configRoot);
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
            root.getModel().add( ModelFactory.createModelForGraph(closure) );
        }
    }

}
