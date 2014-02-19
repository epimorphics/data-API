/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.List;

import com.epimorphics.json.JSFullWriter;

public class Array_Value extends ResultValue {
	
	final List<ResultValue> values;
	
	public Array_Value(List<ResultValue> values) {
		this.values = values;
	}
	
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( "[" );
		for (ResultValue v: values) sb.append(" ").append(v);
		sb.append( " ]" );
		return sb.toString();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Array_Value && same( (Array_Value) other);
	}

	private boolean same(Array_Value other) {
		return values.equals(other.values);
	}

	@Override public void writeTo(JSFullWriter out) {
		out.startArray();
		for (ResultValue v: values) v.writeElement(out);
		out.finishArray();
	}
}