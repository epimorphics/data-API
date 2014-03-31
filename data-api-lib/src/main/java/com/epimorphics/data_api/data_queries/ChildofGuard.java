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
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;

public class ChildofGuard implements Guard {
	    
    final Term parent;
    final Hierarchy h;
    
    public ChildofGuard(Term parent, Hierarchy h) {
    	this.parent = parent;
    	this.h = h;
    }

    @Override public boolean supplantsBaseQuery() {
        return true;
    }

    @Override public String queryFragment(API_Dataset dataset) {
        if (parent == null){
            return h.getTopRootsQuery("item");
        } else {
            return parent.asSparqlTerm(dataset.getPrefixes()) + " " + h.getChildQueryFragment() + " ?item .";
        }}

    @Override public boolean needsDistinct() {
        return h.getNeedsDistinct();
    }

}
