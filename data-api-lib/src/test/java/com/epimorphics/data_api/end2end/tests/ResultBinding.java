package com.epimorphics.data_api.end2end.tests;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

public class ResultBinding {
	final String name;
	final RDFNode node;
	
	public ResultBinding(String name, RDFNode node) {
		this.name = name;
		this.node = node;
	}

	public static ResultBinding parseBinding(String element) {
		String [] parts = element.split("=");
		String name = parts[0];
		RDFNode node = parseNode(parts[1]);
		return new ResultBinding(name, node);
	}

	private static RDFNode parseNode(String s) {
		if (s.startsWith("'")) {
			int limit = s.length() - 1;
			return ResourceFactory.createPlainLiteral(s.substring(1, limit));
		}
		return ResourceFactory.createResource(s);
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof ResultBinding && same((ResultBinding) other);
	}
	
	@Override public int hashCode() {
		return name.hashCode() ^ node.hashCode();
	}

	private boolean same(ResultBinding other) {
		return name.equals(other.name) && node.equals(other.node);
	}
	
	@Override public String toString() {
		return name + " --> " + node + "\n";
	}
}