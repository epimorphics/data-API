/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.math.BigDecimal;

import com.epimorphics.data_api.conversions.ResultValue.Primitive_Value;
import com.epimorphics.json.JSFullWriter;

public class Decimal_Value extends Primitive_Value {
	
	public Decimal_Value(String spelling) {
		super(spelling);
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Decimal_Value && alike( (Decimal_Value) other );
	}

	@Override public void writeMember(String key, JSFullWriter jw) {
		jw.pair(key, new BigDecimal(spelling));
	}

	@Override public void writeElement(JSFullWriter jw) {
		// TODO jw.arrayElement(new BigDecimal(spelling));
		throw new UnsupportedOperationException();
	}
}