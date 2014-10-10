/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.data_queries.terms.TermArray;
import com.epimorphics.data_api.data_queries.terms.TermBool;
import com.epimorphics.data_api.data_queries.terms.TermLanguaged;
import com.epimorphics.data_api.data_queries.terms.TermNumber;
import com.epimorphics.data_api.data_queries.terms.TermResource;
import com.epimorphics.data_api.data_queries.terms.TermString;
import com.epimorphics.data_api.data_queries.terms.TermTyped;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.BrokenException;

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
	
	public Substitution(Problems p, Filter f) {
		Term value = null;
		Aspect aspect = f.a;
		boolean canReplace = false;
		Resource type = aspect.getRangeType();
		if (f.range.op.equals(Operator.EQ)) {
			value = f.range.operands.get(0);
			if (type == null) {
				// rely on suitability of values, ie let through those
				// values which will be identical with values equal to 
				// them.
				if (value instanceof TermResource || value instanceof TermBool) {
					canReplace = true;
				} else if (value instanceof TermString || value instanceof TermTyped || value instanceof TermLanguaged || value instanceof TermArray) {
					value = null;
				} else if (value instanceof TermNumber) {
					value = null;
				} else {
					p.add("Unhandled value " + value);
					value = null;
				}
			} else {
				throw new BrokenException("value rewriting for typed aspects not implemented yet.");
			}
		}
		this.value = value;
		this.canReplace = canReplace;
		this.aspect = aspect;
	}
}