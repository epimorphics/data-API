/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import com.epimorphics.data_api.conversions.ResultValue.Primitive_Value;
import com.epimorphics.json.JSFullWriter;

public class Double_Value extends Primitive_Value {
	
	public Double_Value(String spelling) {
		super(spelling);
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Double_Value && alike( (Double_Value) other );
	}

	@Override public void writeMember(String key, JSFullWriter jw) {
		jw.pair(key, Double.parseDouble(spelling));
	}

	@Override public void writeElement(JSFullWriter jw) {
		jw.arrayElement(Double.parseDouble(spelling));
	}
}