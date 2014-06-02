/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.sparql.SQ.FilterSQ;
import com.epimorphics.data_api.sparql.SQ.WhereElement;

public final class NegatedOptionalAspect extends Constraint  {
	
	final Filter negated;
	
	public NegatedOptionalAspect(Filter negated) {
		this.negated = negated;
	}
	
	public static class Element implements WhereElement {

		final Filter negated;
		final Context cx;
		
		public Element(Context cx, Filter negated) {
			this.cx = cx;
			this.negated = negated;
		}
		
		@Override public void toString(StringBuilder sb, String indent) {
			FilterSQ f = negated.range.asFilterSQ(negated.a);
			
			sb.append(indent).append("FILTER(" );
			
			f.toStringNoFILTER(sb);
			
			sb.append(" || ");
			sb.append(" !BOUND(").append(negated.a.asVar()).append(")");
			sb.append(")").append(nl);
		}		
	}
	
	public void tripleFiltering(Context cx) {
		cx.sq.addWhereElement(new Element(cx, negated));
	}

	@Override public String toString() {
		return "(" + negated + " | UNBOUND(" + negated.a + ")" + ")";
	}

	@Override protected boolean same(Constraint other) {
		NegatedOptionalAspect o = (NegatedOptionalAspect) other;
		return negated.equals(o.negated);
	}

	@Override public Constraint negate() {
		List<Constraint> operands = new ArrayList<Constraint>();
		operands.add(negated.negate());
		return and(operands);
	}
}