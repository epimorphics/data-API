/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.sparql;

import java.util.ArrayList;
import java.util.List;


public class SQ_Where {

	final List<SQ_WhereElement> elements = new ArrayList<SQ_WhereElement>();
	
	public void toString(StringBuilder sb, String indent) {
		for (SQ_WhereElement e: elements)
			e.toSparqlStatement(sb, indent);
	}

	public void add(SQ_WhereElement e) {
		elements.add(e);
	}

	public void addBind(SQ_Expr value, SQ_Variable var) {
		elements.add(new SQ_Bind(value, var));
	}

	public void addTriple(SQ_Triple t) {
		addUnlessPresent(t);
	}

	public void addOptionalTriple(SQ_Triple t) {
		addUnlessPresent(t.optional());
	}
	
	public void addOptionalTriples(List<SQ_Triple> ts) {
		addUnlessPresent(SQ_Triple.optionals(ts));
	}

	public void addFilter(SQ_Filter f) {
		elements.add(f);
	}
	
	public void addComment(String c) {
		elements.add(new SQ_Comment(c));
	}
	
	private void addUnlessPresent(SQ_WhereElement e) {
		System.err.println(">> addUnlessPresent: " + e );
		for (SQ_WhereElement el: elements) {
			if (el.equals(e)) {
				System.err.println(">> YAY it is here alreadies, we're done." );
				return;
			} else {
				System.err.println(">> not " + el);
			}
		}
		System.err.println(">> not already in, adding." );
		elements.add(e);
		
//		if (elements.contains(e)) {
//			System.err.println(">> Noting: element " + e + " is already installed.");
//		} else {
//			System.err.println(">> note: " + e + " not already in " + elements);
//			elements.add(e);
//		}	
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