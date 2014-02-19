/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import com.epimorphics.data_api.conversions.ResultValue.Primitive_Value;
import com.epimorphics.json.JSFullWriter;

public class Boolean_Value extends Primitive_Value {
	
	final boolean value;
	
	public Boolean_Value(boolean value) {
		super(value ? "true" : "false");
		this.value = value;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Boolean_Value && alike( (Boolean_Value) other );
	}

	@Override public void writeMember(String key, JSFullWriter jw) {
		jw.pair(key, value);
	}

	@Override public void writeElement(JSFullWriter jw) {
		jw.arrayElement(value);
	}
}