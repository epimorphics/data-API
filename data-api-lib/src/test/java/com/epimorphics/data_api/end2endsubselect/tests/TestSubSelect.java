package com.epimorphics.data_api.end2endsubselect.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.junit.Test;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestSubSelect {

	API_Dataset dataset = createDataset();
	Model data = createData();
	
	@Test public void testIt() {
		String incoming = "{}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery dq = DataQueryParser.Do(p, dataset, jo);
		String sparql = dq.toSparql(p, dataset);
		
		System.err.println(">> query is:\n" + sparql);
		
		Set<Set<Binding>> obtained = new HashSet<Set<Binding>>();
		
		Query q = QueryFactory.create(sparql);
		QueryExecution qx = QueryExecutionFactory.create(q, data);
		ResultSet rs = qx.execSelect();
		while (rs.hasNext()) {
			Set<Binding> row = new HashSet<Binding>();
			QuerySolution qs = rs.next();
			Iterator<String> names = qs.varNames();
			while(names.hasNext()) {
				String name = names.next();
				RDFNode node = qs.get(name);
				row.add(new Binding(name, node));
			}
			obtained.add(row);
		}
		
		Set<Set<Binding>> expected = parseRows
			( BunchLib.join
				( "  item=eh:/A pre_P='AP' pre_Q='AQ'"
				, "; item=eh:/B pre_P='BP' pre_Q='BQ'"
				, "; item=eh:/C pre_P='CP'"
				, "; item=eh:/D pre_P='DP'"
				));
			;
				
		assertEquals(expected, obtained);
	}
	
	private Set<Set<Binding>> parseRows(String rows) {
		Set<Set<Binding>> result = new HashSet<Set<Binding>>();
		
		String scan = rows;
		while(true) {
			int semi = scan.indexOf(';');
			if (semi < 0) break;
			String row = scan.substring(0, semi).trim();			
			result.add(parseRow(row));
			scan = scan.substring(semi + 1);
		}
		result.add(parseRow(scan));
		
		return result;
	}

	private Set<Binding> parseRow(String row) {		
		Set<Binding> result = new HashSet<Binding>();
		for (String element: row.trim().split("[ \n]+")) {
			result.add(parseBinding(element));
		}		
		return result;
	}

	private Binding parseBinding(String element) {
		String [] parts = element.split("=");
		String name = parts[0];
		RDFNode node = parseNode(parts[1]);
		return new Binding(name, node);
	}

	private RDFNode parseNode(String s) {
		if (s.startsWith("'")) {
			int limit = s.length() - 1;
			return ResourceFactory.createPlainLiteral(s.substring(1, limit));
		}
		return ResourceFactory.createResource(s);
	}

	static final class Binding {
		final String name;
		final RDFNode node;
		
		public Binding(String name, RDFNode node) {
			this.name = name;
			this.node = node;
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof Binding && same((Binding) other);
		}
		
		@Override public int hashCode() {
			return name.hashCode() ^ node.hashCode();
		}

		private boolean same(Binding other) {
			return name.equals(other.name) && node.equals(other.node);
		}
		
		@Override public String toString() {
			return name + " --> " + node + "\n";
		}
	}

	private API_Dataset createDataset() {
		PrefixMapping pm = PrefixMapping.Factory.create();
		pm.setNsPrefix("pre", "eh:/");

		Model c = ModelFactory.createDefaultModel();
		c.setNsPrefixes(pm);
		Resource config = c.createResource("eh:/root");
		API_Dataset result = new API_Dataset(config, (DSAPIManager)(null));
		Aspect p = new Aspect(pm, "pre:P");
		Aspect q = new Aspect(pm, "pre:Q");
		result.add(p).add(q);
		q.setIsOptional(true);
		return result;
	}

	private Model createData() {
		Model result = ModelFactory.createDefaultModel();
		Resource A = result.createResource("eh:/A");
		Resource B = result.createResource("eh:/B");
		Resource C = result.createResource("eh:/C");
		Resource D = result.createResource("eh:/D");
		
		Property P = result.createProperty("eh:/P");
		Property Q = result.createProperty("eh:/Q");
		
		result.add(A, P, result.createLiteral("AP"));
		result.add(A, Q, result.createLiteral("AQ"));
		
		result.add(B, P, result.createLiteral("BP"));
		result.add(B, Q, result.createLiteral("BQ"));
		
		result.add(C, P, result.createLiteral("CP"));
		
		result.add(D, P, result.createLiteral("DP"));
		
		return result;
	}
	
	
}
