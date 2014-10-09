/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;

/**
 	The default implementation of A @eq V is as a
 	FILTER(A'sVariable = V). Where possible this is
 	optimised by using V as the value in the pattern
 	triple (?item A'sProperty A'sValue). Earlier versions
 	of the DSAPI did this substitution with no questions asked,
 	but in fact it is not sound because the value 17 is = to
 	many numeric values that it will not match as a pattern
 	object.
 	
 	Substitution is the class that detects use of @eq
 	and decides if FILTER(=) can be replaced by inline
 	substitution. The easiest case is when the @eq value
 	is a Resource node, in which case = and match are the
 	same.
*/
public class Substitution {
	public final boolean canReplace;
	public final Aspect aspect;
	public final Term value;
	
	public Substitution(Filter f) {
		aspect = f.a;
		if (f.range.op.equals(Operator.EQ)) {
			value = f.range.operands.get(0);
			canReplace = true;
		} else {
			value = null;
			canReplace = false;
		}
	}
}