/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

public class DataQuery {
	
	final List<Filter> filters;
	
	public DataQuery(List<Filter> filters) {
		this.filters = filters;
	}
	
	public List<Sort> sorts() {
		return new ArrayList<Sort>();
	}
	
	public List<Filter> filters() {
		return filters;
	}
	
	public String lang() {
		return null;
	}
	
	public Slice slice() {
		return new Slice();
	}
}