/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.List;

import com.epimorphics.data_api.libs.BunchLib;

public class Range {
	
	final String op;
	final List<Value> operands;
	
	public Range(String op, List<Value> operands ) {
		this.op = op;
		this.operands = operands;
	}
	
	public static Range EQ(Value v) {
		return new Range("eq", BunchLib.list(v));
	}
	
	@Override public String toString() {
		return "<" + op + " " + joinStrings(operands) + ">";
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Range && same( (Range) other );
	}
	
	private boolean same(Range other) {
		return op.equals(other.op) && operands.equals(other.operands); 
	}

	String joinStrings(List<Value> operands) {
		StringBuilder sb = new StringBuilder();
		String comma = "";
		for (Value v: operands) {
			sb.append(comma).append(v.unwrap());
			comma = ", ";
		}
		return sb.toString();
	}
	
}