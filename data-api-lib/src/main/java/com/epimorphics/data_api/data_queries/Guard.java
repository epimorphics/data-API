/******************************************************************
 * File:        Guard.java
 * Created by:  Dave Reynolds
 * Created on:  14 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.datasets.API_Dataset;

/**
 * Represents a data-set wide filter, not related to a specific aspect.
 * If a guard is present it may replace the base query.
 */
public interface Guard {
    
    public boolean supplantsBaseQuery();
    
    public boolean needsDistinct();

    public String queryFragment(API_Dataset dataset);
}
