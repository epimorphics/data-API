/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import com.epimorphics.data_api.conversions.ResultValue.Base_Value;
import com.epimorphics.json.JSFullWriter;

public class URI_Value extends Base_Value {
	
	public URI_Value(String spelling) {
		super(spelling);
	}
	
	@Override public String toString() {
		return "<" + spelling + ">";
	}

	@Override public void writeTo(JSFullWriter jw) {
		jw.startObject();
		jw.pair("@id", spelling);
		jw.finishObject();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof URI_Value && alike( (URI_Value) other );
	}
}