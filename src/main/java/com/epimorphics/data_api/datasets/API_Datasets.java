/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets;

import java.util.HashSet;
import java.util.Set;

import com.epimorphics.data_api.datasets.tests.TestAPI_Datasets;

public class API_Datasets {

	final Set<API_Dataset> datasets = new HashSet<API_Dataset>();
	
	public Set<API_Dataset> getDatasets() {
		return datasets;
	}

	public void add(API_Dataset d) {
		datasets.add(d);
	}
	
}