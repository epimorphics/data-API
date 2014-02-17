/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import com.epimorphics.data_api.conversions.ResultValue.Base_Value;
import com.epimorphics.json.JSFullWriter;

public class Literal_Value extends Base_Value {
	
	final String subKey;
	final String subValue;
	
	public Literal_Value(String spelling, String subKey, String subValue) {
		super(spelling);
		this.subKey = subKey;
		this.subValue = subValue;
	}

	@Override public void writeTo(JSFullWriter jw) {
		jw.startObject();
		jw.pair("@value", spelling);
		jw.pair(subKey, subValue);
		jw.finishObject();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Literal_Value && same( (Literal_Value) other );
	}

	private boolean same(Literal_Value other) {
		return 
			alike(other)
			&& subKey.equals(other.subKey) && subValue.equals(other.subValue)
			;
	}
}