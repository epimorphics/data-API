/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

public class Slice {

	final Integer length;
	final Integer offset;
	
	public Slice(Integer length, Integer offset) {
		this.length = length;
		this.offset = offset;
	}
	
	public static Slice create(Integer length) {
		return new Slice(length, null);
	}
	
	public static Slice create(Integer length, Integer offset) {
		return new Slice(length, offset);
	}
	
	public boolean isAll() {
		return length == null && offset == null;
	}

	public static Slice all() {
		return new Slice(null, null);
	}
	
	@Override public String toString() {
		return
			"<slice "
			+ (length == null ? "ALL" : length)
			+ " "
			+ (offset == null ? "" : " FROM " + offset)
			+ ">"
			;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Slice && same( (Slice) other );
	}

	private boolean same(Slice other) {
		return same(length, other.length) && same(offset, other.offset);
	}

	private boolean same(Integer a, Integer b) {
		return a == null ? b == null : a.equals(b);
	}
	
	
}