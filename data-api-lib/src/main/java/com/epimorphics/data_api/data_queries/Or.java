/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.Constraint.State;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ_WhereElement;
import com.hp.hpl.jena.shared.BrokenException;

public class Or extends Bool {
	
	public Or(List<Constraint> operands) {
		super(operands);
	}
	
	void doAspect(State s, Aspect a) {
		
		final List<SQ> new_operands = new ArrayList<SQ>(operands.size());
		
		for (Constraint x: operands) {
			SQ sq = new SQ();
			StringBuilder out = new StringBuilder();
			Context inner_cx = new Context( sq, out, s.cx.dq, s.cx.p, s.cx.api );
			x.translate(inner_cx.p, inner_cx);
			new_operands.add(sq);
		}
		SQ_WhereElement e = new SQ_WhereElement() {

			@Override public void toSparqlStatement(StringBuilder sb, String indent) {
				String gap = "";
				for (SQ o: new_operands) {
					sb.append(indent).append(gap).append(nl); 
					gap = " UNION ";
					sb.append(indent).append("{");
					o.toString(sb, indent + "  ");
					sb.append(indent).append("}").append(nl);
				}
			}};
		s.cx.sq.addWhereElement(e);	
	}

	public void tripleFiltering(Context cx) {
		
		final List<SQ> new_operands = new ArrayList<SQ>(operands.size());
		
		for (Constraint x: operands) {
			SQ sq = new SQ();
			StringBuilder out = new StringBuilder();
			Context inner_cx = new Context( sq, out, cx.dq, cx.p, cx.api );
			x.translate(inner_cx.p, inner_cx);
			new_operands.add(sq);
		}
		SQ_WhereElement e = new SQ_WhereElement() {

			@Override public void toSparqlStatement(StringBuilder sb, String indent) {
				String gap = "";
				for (SQ o: new_operands) {
					sb.append(indent).append(gap).append(nl); 
					gap = " UNION ";
					sb.append(indent).append("{");
					o.toString(sb, indent + "  ");
					sb.append(indent).append("}").append(nl);
				}
			}};
		cx.sq.addWhereElement(e);
	}
	
	@Override public Constraint negate() {
		List<Constraint> newOperands = new ArrayList<Constraint>();
		for (Constraint y: operands) newOperands.add(y.negate());
		return and(newOperands);
	}
}