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

public class DatasetMonitor extends ConfigMonitor<API_Dataset>{

    @Override
    protected API_Dataset configure(File file) {
        // TODO Auto-generated method stub
        return null;
    }

}
