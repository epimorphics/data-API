/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.libs;

import java.util.Arrays;
import java.util.List;

// "Bunch" to cover List and Set with few syllables.
public class BunchLib {

	/**
		list(x...) returns a list(T) whose elements are x...		
	*/
	@SafeVarargs public static <T> List<T> list( T... elements ) {
		return Arrays.asList( elements );
	}

}
