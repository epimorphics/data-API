/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import com.epimorphics.data_api.conversions.ResultValue.Primitive_Value;
import com.epimorphics.json.JSFullWriter;

public class String_Value extends Primitive_Value {
	
	public String_Value(String spelling) {
		super(spelling);
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof String_Value && alike( (String_Value) other );
	}
	
	@Override public String toString() {
		return "'" + spelling + "'";
	}

	@Override public void writeMember(String key, JSFullWriter jw) {
		jw.pair(key, spelling);
	}

	@Override public void writeElement(JSFullWriter jw) {
		jw.arrayElement(spelling);
	}
}