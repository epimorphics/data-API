/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQ_Where {

	final List<SQ_WhereElement> elements = new ArrayList<SQ_WhereElement>();
	final List<SQ_Bind> bindingElements = new ArrayList<SQ_Bind>();
	
	public void toString(StringBuilder sb, String indent) {
		for (SQ_WhereElement e: elements) e.toSparqlStatement(sb, indent);
		for (SQ_Bind e: bindingElements) e.toSparqlStatement(sb, indent);
	}

	public void add(SQ_WhereElement e) {
		elements.add(e);
	}

	private final Map<SQ_Variable, SQ_Node> equals = new HashMap<SQ_Variable, SQ_Node>();
	
	public void addBind(SQ_Node value, SQ_Variable var) {
		SQ_Bind b = new SQ_Bind(value, var);
		bindingElements.add(b);
		equals.put(var, value);
	}

	// special-case a text-query triple to go at the front
	public void addTriple(SQ_Triple t) {
		t = subst(t);
		if (t.P.equals(SQ_Const.textQuery)) {
			elements.add(0, t);
		} else {
			addUnlessPresent(t);
		}
	}

	public void addOptionalTriple(SQ_Triple t) {
		addUnlessPresent(subst(t).optional());
	}
	
	public void addOptionalTriples(List<SQ_Triple> ts) {
		List<SQ_Triple> pruned = new ArrayList<SQ_Triple>();
		for (SQ_Triple t: ts) {
			t = subst(t);
			if (!elements.contains(t))
				pruned.add(t);
		}
		if (pruned.size() > 0)
			addUnlessPresent(SQ_Triple.optionals(pruned));
	}
	
	private SQ_Triple subst(SQ_Triple t) {
		return new SQ_Triple(subst(t.S), subst(t.P), subst(t.O));
	}

	private SQ_Node subst(SQ_Node s) {
		SQ_Node value = equals.get(s);
		return value == null ? s : value;
	}

	public void addFilter(SQ_Filter f) {
		elements.add(f);
	}
	
	public void addComment(String c) {
		elements.add(new SQ_Comment(c));
	}
	
	/**
		add an element e to elements unless it's already present.
	*/
	private void addUnlessPresent(SQ_WhereElement e) {
//		System.err.println(">> addUnlessPresent: " + e );
//		for (SQ_WhereElement el: elements) {
//			if (el.equals(e)) {
//				System.err.println(">> YAY it is here alreadies, we're done." );
//				return;
//			} else {
//				System.err.println(">> not " + el);
//			}
//		}
//		System.err.println(">> not already in, adding." );
		
		// if (!elements.contains(e)) elements.add(e);
		
		for (SQ_WhereElement el: elements) {
			// System.err.println(">> " + e + " equals existing " + el.getClass().getSimpleName() + " " + el + ": " + (el.equals(e) ? "yes" : "no"));
			if (el.equals(e) || SPLiteralAlreadyExistsForThisSPVariable(el, e)) return;
		}
		elements.add(e);	
	}

	private boolean SPLiteralAlreadyExistsForThisSPVariable(SQ_WhereElement present, SQ_WhereElement toInsert) {
		if (present instanceof SQ_Triple && toInsert instanceof SQ_Triple) {
			SQ_Triple pt = (SQ_Triple) present;
			SQ_Triple it = (SQ_Triple) toInsert;
			if (pt.S.equals(it.S) && pt.P.equals(it.P)) {
				if (isLiteral(pt.O) && it.O instanceof SQ_Variable) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isLiteral(SQ_Node o) {
		return o instanceof SQ_Literal || o instanceof SQ_TermAsNode;
	}

	static class SubQuery implements SQ_WhereElement {

		final SQ subQuery;
		
		public SubQuery(SQ subQuery) {
			this.subQuery = subQuery;
		}
		
		@Override public void toSparqlStatement(StringBuilder sb, String indent) {
			sb.append(indent).append("{").append("\n");
			subQuery.toString(sb, indent + "  ");
			sb.append(indent).append("}").append("\n");
		}
		
	}
	
	public void addSubquery(SQ nested) {
		elements.add(new SubQuery(nested));
	}
	
}