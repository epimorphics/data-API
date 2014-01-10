/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

/**
    A Value wraps an Object. Only a limited set of classes of object
    can be Wrapped; they correspond to values that can conveniently be
    converted to SPARQL terms.
*/
public class Value {
	
	final Object wrapped;
	
	public Value(Object x) {
		this.wrapped = x;			
	}
	
	public static Value wrap(Object x) {
		return new Value(x);
	}
	
	public Object unwrap() {
		return wrapped;
	}
	
	@Override public String toString() {
		return wrapped.toString();
	}
	
	@Override public int hashCode() {
		return wrapped.hashCode();
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Value && wrapped.equals(((Value) other).wrapped);
	}
}