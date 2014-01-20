/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.libs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// "Bunch" to cover List and Set with few syllables.
public class BunchLib {

	/**
		list(x...) returns a list(T) whose elements are x...		
	*/
	@SafeVarargs public static <T> List<T> list( T... elements ) {
		return Arrays.asList( elements );
	}

	/**
	    set(x...) returns a set(T) whose elements are the given elements.
	*/
	@SafeVarargs public static <T> Set<T> set(T... elements) {
		Set<T> result = new HashSet<T>();
		for (T e: elements) result.add(e);
		return result;
	}

	public static String join(String...strings) {
		StringBuilder sb = new StringBuilder();
		for (String s: strings) sb.append(s).append("\n");
		return sb.toString();
	}

	public static String join(List<String> strings) {
		StringBuilder sb = new StringBuilder();
		for (String s: strings) sb.append(s).append("\n");
		return sb.toString();
	}

}
