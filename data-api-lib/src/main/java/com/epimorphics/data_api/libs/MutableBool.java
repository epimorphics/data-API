/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.libs;

/**
    MutableBool is a structure with a single mutable boolean value
    in it. Useful when a mere variable isn't sufficient, eg for
    a boolean that is changed inside an anonymous class.
*/
public final class MutableBool {
	public boolean value;
}