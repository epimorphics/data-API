/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.HashMap;
import java.util.Map;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;

/**
    A Row implements a row of a result-set as a map from
    element names to Values.
*/
public class Row implements JSONWritable {

	Map<String, ResultValue> members = new HashMap<String, ResultValue>();
	
	@Override public void writeTo(JSFullWriter jw) {
		jw.startObject();
		for (Map.Entry<String, ResultValue> e: members.entrySet()) {
			ResultValue v = e.getValue();
			if (v == null) {  } else { v.writeTo(jw); }
		}
		jw.finishObject();			
	}
	
	public void put(String key, ResultValue value) {
		members.put(key, value);
	}
	
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[[");
		for (Map.Entry<String, ResultValue> e: members.entrySet()) {
			sb.append( " " ).append(e.getKey()).append(": ").append(e.getValue());
		}
		sb.append(" ]]");
		return sb.toString();			
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Row && same((Row) other);
	}

	private boolean same(Row other) {
		return members.equals(other.members);
	}
	
}