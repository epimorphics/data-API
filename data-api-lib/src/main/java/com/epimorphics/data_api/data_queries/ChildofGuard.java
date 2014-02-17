/******************************************************************
 * File:        ChildofGuard.java
 * Created by:  Dave Reynolds
 * Created on:  14 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.config.Hierarchy;
import com.epimorphics.data_api.datasets.API_Dataset;

public class ChildofGuard implements Guard {
    String queryFragment;
    boolean needsDistinct;
    
    public ChildofGuard(Term parent, Hierarchy h) {
        if (parent == null){
            queryFragment = h.getTopRootsQuery("item");
        } else {
            queryFragment = parent.asSparqlTerm() + " " + h.getChildQueryFragment() + " ?item .";
        }
        needsDistinct = h.getNeedsDistinct();
    }

    @Override
    public boolean supplantsBaseQuery() {
        return true;
    }

    @Override
    public String queryFragment(API_Dataset dataset) {
        return queryFragment;
    }

    @Override
    public boolean needsDistinct() {
        return needsDistinct;
    }

}
