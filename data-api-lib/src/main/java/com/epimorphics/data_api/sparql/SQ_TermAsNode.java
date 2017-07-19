/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.sparql;

import com.epimorphics.data_api.data_queries.terms.Term;
import org.apache.jena.shared.PrefixMapping;

public final class SQ_TermAsNode extends SQ_Node {
	private final PrefixMapping pm;
	private final Term equalTo;

	public SQ_TermAsNode(PrefixMapping pm, Term equalTo) {
		this.pm = pm;
		this.equalTo = equalTo;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof SQ_TermAsNode && same((SQ_TermAsNode) other);
	}
	
	@Override public int hashCode() {
		return equalTo.hashCode();
	}

	private boolean same(SQ_TermAsNode other) {
		return equalTo.equals(other.equalTo);
	}

	@Override public void toSparqlExpr(StringBuilder sb) {
		sb.append(equalTo.asSparqlTerm(pm));

	}
}