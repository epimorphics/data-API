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
import com.epimorphics.data_api.end2end.tests.ResultBinding;
import com.epimorphics.data_api.end2end.tests.QueryTestSupport;
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
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestSubSelect {

	API_Dataset dataset = createDataset();
	Model data = createData();
	
	@Test public void testUnconstrainedQuery() {
		String incoming = "{}";
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery dq = DataQueryParser.Do(p, dataset, jo);
		String sparql = dq.toSparql(p, dataset);
		
		System.err.println(">> query is:\n" + sparql);
		
		Set<Set<ResultBinding>> obtained = new HashSet<Set<ResultBinding>>();
		
		Query q = QueryFactory.create(sparql);
		QueryExecution qx = QueryExecutionFactory.create(q, data);
		ResultSet rs = qx.execSelect();
		while (rs.hasNext()) {
			Set<ResultBinding> row = new HashSet<ResultBinding>();
			QuerySolution qs = rs.next();
			Iterator<String> names = qs.varNames();
			while(names.hasNext()) {
				String name = names.next();
				RDFNode node = qs.get(name);
				row.add(new ResultBinding(name, node));
			}
			obtained.add(row);
		}
		
		Set<Set<ResultBinding>> expected = QueryTestSupport.parseRows
			( BunchLib.join
				( "  item=eh:/A pre_P='AP' pre_Q='AQ'"
				, "; item=eh:/B pre_P='BP' pre_Q='BQ'"
				, "; item=eh:/C pre_P='CP'"
				, "; item=eh:/D pre_P='DP'"
				));
			;
				
		assertEquals(expected, obtained);
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
