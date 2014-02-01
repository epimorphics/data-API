/******************************************************************
 * File:        DSAPIManager.java
 * Created by:  Dave Reynolds
 * Created on:  28 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config;

import java.util.Collection;
import java.util.Iterator;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;

/**
 * An appbase component which manages a set of DSAPI dataset end points.
 * <p>
 * To configure an instance of this need at least a (sparql) source, apiBase
 * and a DatasetMonitor which manages the scanning of a directory for configuration instances.
 * The apiBase defines the root of all the apis provided by this manager instance. Typically
 * this will be a server relative context path ("/dsapi/foo").
 * </p><p>
 * The DatasetMonitor is separately configured to allow easy setting of parameters like productionMode.
 * </p><p>
 * The json representation is an array of data set descriptions.
 * </p>
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class DSAPIManager extends ComponentBase {
    protected SparqlSource source;
    protected DatasetMonitor monitoredDatasets;
    protected String apiBase;

    public SparqlSource getSource() {
        return source;
    }

    public void setSource(SparqlSource source) {
        this.source = source;
    }

    public String getApiBase() {
        return apiBase;
    }

    public void setApiBase(String apiBase) {
        this.apiBase = apiBase;
    }

    public void setMonitoredDatasets(DatasetMonitor monitoredDatasets) {
        this.monitoredDatasets = monitoredDatasets;
        monitoredDatasets.setManager(this);
    }

    public Collection<API_Dataset> getDatasets() {
        return monitoredDatasets.getEntries();
    }
    
    public API_Dataset getDataset(String name) {
        return monitoredDatasets.get(name);
    }

    public JSONWritable asJson(String lang) {
        return new Writer(lang);
    }

    public void writeJson(JSFullWriter out, String lang) {
        out.startArray();
        for (Iterator<API_Dataset> i = getDatasets().iterator(); i.hasNext();) {
            i.next().writeShortTo(out, lang);
            if (i.hasNext()) {
                out.arraySep();
            }
        }
        out.finishArray();
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
