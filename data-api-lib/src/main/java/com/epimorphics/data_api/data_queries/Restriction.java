/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2015 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;


/**
	A Restriction is a constraint that depends on
	the value of an aspect, or is a global search.
*/
public abstract class Restriction extends Constraint {

	/**
		Add filters and patterns to the State s to
		reflect the value and operation of this aspect.
	*/
	abstract void applyTo(State s);
}
