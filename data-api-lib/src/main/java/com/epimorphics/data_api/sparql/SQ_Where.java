/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.shared.BrokenException;

public class SQ_Where {
	
	final List<SQ_WhereElement> textQueries = new ArrayList<SQ_WhereElement>();
	final List<SQ_WhereElement> groundTriples = new ArrayList<SQ_WhereElement>();
	final List<SQ_WhereElement> ungroundTriples = new ArrayList<SQ_WhereElement>();
	final List<SQ_WhereElement> optionalTriples = new ArrayList<SQ_WhereElement>();
	final List<SQ_WhereElement> filterElements = new ArrayList<SQ_WhereElement>();
	final List<SQ_WhereElement> otherElements = new ArrayList<SQ_WhereElement>();
	final List<SQ_Bind> bindingElements = new ArrayList<SQ_Bind>();
	final List<SQ_Expr> sqFilters = new ArrayList<SQ_Expr>();
	
	final Set<SQ_WhereElement> addedTriples = new HashSet<SQ_WhereElement>();
	
	public void toString(StringBuilder sb, String indent, SQ parent) {
//		for (SQ_WhereElement e: textQueries) e.toSparqlStatement(sb, indent);
//		for (SQ_WhereElement e: groundTriples) e.toSparqlStatement(sb, indent);
//		for (SQ_WhereElement e: ungroundTriples) e.toSparqlStatement(sb, indent);
//		for (SQ_WhereElement e: filterElements) e.toSparqlStatement(sb, indent);
//		for (SQ_WhereElement e: otherElements) e.toSparqlStatement(sb, indent);
//		for (SQ_WhereElement e: optionalTriples) e.toSparqlStatement(sb, indent);
//		for (SQ_WhereElement e: optionalFilterElements) e.toSparqlStatement(sb, indent);
//		for (SQ_Bind e: bindingElements) e.toSparqlStatement(sb, indent);
		
		int countBefore = textQueries.size() + groundTriples.size() + ungroundTriples.size() + filterElements.size();
		int countAfter = sizeWithoutComments(otherElements) + optionalTriples.size() + sqFilters.size() + bindingElements.size();
		
		boolean nest = countBefore > 0 && countAfter > 0;
		boolean subSelect = true;
		
//		System.err.println(">> textQueries: " + textQueries);
//		System.err.println(">> grountTriples: " + groundTriples);
//		System.err.println(">> ungroundTriples: " + ungroundTriples);
//		System.err.println(">> filterElements: " + filterElements);
//
//		System.err.println();
//		
//		System.err.println(">> otherElements: " + otherElements + " " + otherElements.size());
//		System.err.println(">> optionalTriples: " + optionalTriples + " " + optionalTriples.size());
//		System.err.println(">> sqFilters: " + sqFilters+ " " + sqFilters.size());
//		System.err.println(">> bndingElements: " + bindingElements + " " + bindingElements.size());
//		
//		System.err.println(">> countBefore = " + countBefore);
//		System.err.println(">> countAfter = " + countAfter);
//		System.err.println(">> nest = " + nest);
		
		if (nest) {
			sb.append(indent).append("{").append(SQ.nl);
			if (subSelect) {
				Set<String> varNames = new HashSet<String>();
				update(varNames, textQueries);
				update(varNames, groundTriples);
				update(varNames, ungroundTriples);
				update(varNames, filterElements);
				
//				for (String s: varNames) System.err.println(">> " + s);
				
				
				sb.append(indent).append("SELECT ");
				for (String name: varNames) {
					sb.append(indent).append("  ").append(name).append(SQ.nl);
				}
				sb.append("{");			
			}
			indent += " ";			
		}
		
		section(sb, indent, "text search queries", textQueries);
		section(sb, indent, "items with values EQ to a constant", groundTriples);

		section(sb, indent, "triples with unbound objects", ungroundTriples);
		section(sb, indent, "mandatory filters", filterElements);
		
		if (nest) {
			sb.append(indent).append("}").append(SQ.nl);
			if (subSelect) {
				if (parent.itemModifiers != null) parent.itemModifiers.render(sb);
				sb.append("}");
			}
			indent = indent.substring(2);			
		}
		
		section(sb, indent, "otherwise uncategorised elements", otherElements);
		section(sb, indent, "optional triples", optionalTriples);
		exprSection(sb, indent, "negated aspects", sqFilters); 

		section(sb, indent, "BINDings", bindingElements);
	}
	
	private void update(Set<String> varNames, List<SQ_WhereElement> wheres) {
		for (SQ_WhereElement w: wheres) w.updateVars(varNames);
	}

	private int sizeWithoutComments(List<SQ_WhereElement> others) {
		int count = 0;
		for (SQ_WhereElement o: others) 
			if (!(o instanceof SQ_Comment)) count += 1;
		return count;
	}

	protected void section(StringBuilder sb, String indent, String title, List<? extends SQ_WhereElement> elements) {
		if (elements.size() > 0) {
			sb.append(indent).append("# ").append(title).append("\n");
			for (SQ_WhereElement e: elements) e.toSparqlStatement(sb, indent);
		}
	}
	
	protected void exprSection(StringBuilder sb, String indent, String title, List<? extends SQ_Expr> elements) {
		if (elements.size() > 0) {
			sb.append(indent).append("# ").append(title).append("\n");
			for (SQ_Expr e: elements) {
				sb.append(indent);
				sb.append("FILTER(");
				e.toSparqlExpr(sb);
				sb.append(")");
			}
		}
	}

	public void add(SQ_WhereElement e) {
		otherElements.add(e);
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
			if (true) throw new BrokenException("BOOM");
			textQueries.add(0, t);
		} else if (t.O instanceof SQ_Variable) {
			addUnlessPresent(ungroundTriples, t);
		} else {
			addUnlessPresent(groundTriples, t);
		}
	}
	
	public void addSearchTriple(SQ_Triple t) {
		t = subst(t);
		addUnlessPresent(textQueries, t);
	}

	public void addOptionalTriple(SQ_Triple t) {
		addUnlessPresent(optionalTriples, subst(t).optional());
	}
	
	public void addOptionalTriples(List<SQ_Triple> ts) {
		List<SQ_Triple> pruned = new ArrayList<SQ_Triple>();
		for (SQ_Triple t: ts) {
			SQ_Triple subst_t = subst(t);
			if (!addedTriples.contains(subst_t))
				pruned.add(subst_t);
		}
		if (pruned.size() > 0)
			addUnlessPresent(optionalTriples, SQ_Triple.optionals(pruned));
	}
	
	private SQ_Triple subst(SQ_Triple t) {
		return new SQ_Triple(subst(t.S), subst(t.P), subst(t.O));
	}

	private SQ_Node subst(SQ_Node s) {
		SQ_Node value = equals.get(s);
		return value == null ? s : value;
	}

	public void addSqFilter(SQ_Expr toAdd) {
		sqFilters.add(toAdd);
	}

	public void addFilter(SQ_Filter f) {	
		filterElements.add(f);
	}
	
	public void addComment(String c) {
		otherElements.add(new SQ_Comment(c));
	}
	
	/**
		add an element e to elements unless it's already present.
	*/
	private void addUnlessPresent(List<SQ_WhereElement> addTo, SQ_WhereElement e) {
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
		for (SQ_WhereElement el: addedTriples) {
			// System.err.println(">> " + e + " equals existing " + el.getClass().getSimpleName() + " " + el + ": " + (el.equals(e) ? "yes" : "no"));
			if (el.equals(e) || SPLiteralAlreadyExistsForThisSPVariable(el, e)) return;
		}
		addTo.add(e);
		addedTriples.add(e);
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
	
}