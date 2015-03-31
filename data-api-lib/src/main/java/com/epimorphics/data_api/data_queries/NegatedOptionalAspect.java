/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.sparql.SQ_Bind;
import com.epimorphics.data_api.sparql.SQ_Call;
import com.epimorphics.data_api.sparql.SQ_Expr;
import com.epimorphics.data_api.sparql.SQ_Filter;
import com.epimorphics.data_api.sparql.SQ_Infix;
import com.epimorphics.data_api.sparql.SQ_TermAsNode;
import com.epimorphics.data_api.sparql.SQ_Variable;
import com.epimorphics.data_api.sparql.SQ_WhereElement;
import com.hp.hpl.jena.shared.BrokenException;
import com.hp.hpl.jena.shared.PrefixMapping;

public final class NegatedOptionalAspect extends Constraint  {
	
	final Filter negated;
	
	public NegatedOptionalAspect(Filter negated) {
		this.negated = negated;
	}

	void doAspect(State s, Aspect a) {
		List<Term> terms = negated.range.operands;
		List<SQ_Expr> operands = new ArrayList<SQ_Expr>(terms.size());
		for (Term t: terms) operands.add(new SQ_TermAsNode(s.cx.api.getPrefixes(), t));
		SQ_Filter negFilter = new SQ_Filter(negated.range.op, a, operands);			
		SQ_Expr bound = new SQ_Call("BOUND", BunchLib.list((SQ_Expr) new SQ_Variable(negated.a.asVar().substring(1))));
		SQ_Expr toAdd = new SQ_Infix(negFilter, "||", new SQ_Call("!", BunchLib.list(bound)));
		s.cx.sq.addSqFilter(toAdd);
	}
	
	public static class Element implements SQ_WhereElement {

		final Filter negated;
		final Context cx;
		
		public Element(Context cx, Filter negated) {
			this.cx = cx;
			this.negated = negated;
		}
		
		static final PrefixMapping tobefixed = PrefixMapping.Factory.create();
		
		@Override public void toSparqlStatement(StringBuilder sb, String indent) {
			SQ_Filter f = negated.range.asFilterSQ(tobefixed, negated.a);
			
			sb.append(indent).append("FILTER(" );
			
			f.toStringNoFILTER(sb);
			
			sb.append(" || ");
			sb.append(" !BOUND(").append(negated.a.asVar()).append(")");
			sb.append(")").append(nl);
		}		
	}
	
	public void tripleFiltering(Context cx) {
		// cx.sq.addWhereElement(new Element(cx, negated));
		cx.sq.addOptionalFilter(new Element(cx, negated));
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

	@Override protected boolean constrains(Aspect a) {
		return negated.constrains(a);
	}	
}