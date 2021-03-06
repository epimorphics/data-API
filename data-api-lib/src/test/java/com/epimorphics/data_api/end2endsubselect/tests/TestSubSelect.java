package com.epimorphics.data_api.end2endsubselect.tests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
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

public class TestSubSelect {

	API_Dataset dataset = createPQDataset();
	Model data = createData();
	
	@Test public void testUnconstrainedQuery() {
		String incoming = "{}";
		
		Set<Set<ResultBinding>> expected = QueryTestSupport.parseRows
			( BunchLib.join
				( "  item=eh:/A pre_P='AP' pre_Q='AQ'"
				, "; item=eh:/B pre_P='BP' pre_Q='BQ'"
				, "; item=eh:/C pre_P='CP'"
				, "; item=eh:/D pre_P='DP'"
				));
			;
		
		runQuery(incoming, expected);
	}	
	
	@Test public void testOuterSortedQuery() {
		String incoming = "{'@limit': 1, '@sort': [{'@up': 'pre:P'}]}";
		
		Set<Set<ResultBinding>> expected = QueryTestSupport.parseRows
			( BunchLib.join
				( "  item=eh:/A pre_P='AP' pre_Q='AQ'"
				));
			;
		
		runQuery(incoming, expected);
	}	
	
	@Test public void testInnerSortedQueryItemLimit1() {
		String incoming = "{'@limit': 1, '@sort': [{'@up': 'pre:P'}]}";
		
		Set<Set<ResultBinding>> expected = QueryTestSupport.parseRows
			( BunchLib.join
				( "  item=eh:/A pre_P='AP' pre_Q='AQ'"
				));
			;
		
		runQuery(incoming, expected);
	}	
	
	@Test public void testInnerSortedQueryItemLimit2() {
		String incoming = "{'@limit': 2, '@sort': [{'@up': 'pre:P'}]}";
		
		Set<Set<ResultBinding>> expected = QueryTestSupport.parseRows
			( BunchLib.join
				( "  item=eh:/A pre_P='AP' pre_Q='AQ'"
				, "; item=eh:/B pre_P='BP' pre_Q='BQ'"
				));
			;
		
		runQuery(incoming, expected);
	}	

	@Test public void testOuterSortedQueryItemLimit2() {
		String incoming = "{'@limit': 2, '@sort': [{'@up': 'pre:P'}]}";
		
		Set<Set<ResultBinding>> expected = QueryTestSupport.parseRows
			( BunchLib.join
				( "  item=eh:/A pre_P='AP' pre_Q='AQ'"
				, "; item=eh:/B pre_P='BP' pre_Q='BQ'"
				));
			;
		
		runQuery(incoming, expected);
	}	
	
	@Test public void testOptionalSortReversed() {
		String incoming = "{'@limit': 2, '@sort': [{'@down': 'pre:P'}]}";
		
		Set<Set<ResultBinding>> expected = QueryTestSupport.parseRows
			( BunchLib.join
				( 
				"  item=eh:/C pre_P='CP'"
				, "; item=eh:/D pre_P='DP'"
				));
			;
		
		runQuery(incoming, expected);
	}	
	
	@Test public void testItemOptionalSortReversed() {
		String incoming = "{'@limit': 2, '@sort': [{'@down': 'pre:P'}]}";
		
		Set<Set<ResultBinding>> expected = QueryTestSupport.parseRows
			( BunchLib.join
				( 
				"  item=eh:/C pre_P='CP'"
				, "; item=eh:/D pre_P='DP'"
				));
			;
		
		runQuery(incoming, expected);
	}	
	
	// ----------------------==================-------------------

	private void runQuery(String incoming, Set<Set<ResultBinding>> expected) {
		JsonObject jo = JSON.parse(incoming);
		Problems p = new Problems();
		DataQuery dq = DataQueryParser.Do(p, dataset, jo);
		String sparql = dq.toSparql(p, dataset);
		
		// System.err.println(">> query is:\n" + sparql);
		
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
		assertEquals(expected, obtained);
	}
	
	private API_Dataset createPQDataset() {
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
