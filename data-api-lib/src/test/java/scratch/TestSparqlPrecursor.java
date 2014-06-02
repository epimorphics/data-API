/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package scratch;

import java.util.List;

import org.junit.Test;

import com.epimorphics.data_api.data_queries.Operator;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.data_queries.Sort;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.data_api.sparql.SQ.Expr;
import com.hp.hpl.jena.shared.PrefixMapping;

public class TestSparqlPrecursor {
	
	@Test public void testingPrecursor() {
		
		SQ q = new SQ();
		System.err.println(">>\n" + q.toString());
		
		SQ q2 = new SQ();
		q2.addOutput(new SQ.Variable("?alpha"));
		System.err.println(">>\n" + q2.toString());
		
		SQ q3 = new SQ();
		q3.addOutput(new SQ.Variable("?alpha"));
		q3.addOutput(new SQ.Variable("?beta"));
		System.err.println(">>\n" + q3.toString());
		
	}
	
	@Test public void testing2() {
		SQ q = new SQ();
		q.addOutput(new SQ.Variable("?alpha"));
		SQ.Node S1 = new SQ.Resource("eh:/alpha");
		SQ.Node S2 = new SQ.Resource("eh:/beta");
		SQ.Node P = new SQ.Variable("?item");
		SQ.Node O = new SQ.Literal("eh:/alpha", "xsd:string");
		
		q.addTriple(new SQ.Triple(S1, P, O));
		q.addTriple(new SQ.Triple(S2, P, O));
		
		System.err.println(">>\n" + q.toString());
	}
	
	// Operator op, Variable x, List<Expr> operands
	@Test public void testing3() {
		SQ q = new SQ();
		SQ.Variable A = new SQ.Variable("?A");
		SQ.Node P = new SQ.Resource("eh:/P");
		SQ.Literal V = new SQ.Literal("17", "xsd:integer");
		q.addFilter(new SQ.FilterSQ(Operator.EQ, A, BunchLib.list((Expr) V)));		
		q.addTriple(new SQ.Triple(A, P, V));
		System.err.println(">>\n" + q.toString());

	}
	
	@Test public void testing4() {
		SQ q = new SQ();
		
		q.setLimit(17);
		q.setOffset(42);
		
		System.err.println(">>\n" + q.toString());
	}
	
	@Test public void testing5() {
		SQ q = new SQ();
		PrefixMapping pm = PrefixMapping.Factory.create().setNsPrefix("a", "eh:/A");
		List<Sort> sorts = BunchLib.list(new Sort(new Shortname(pm, "a:b"), true));
		q.addSorts(sorts);
		System.err.println(">>\n" + q.toString());
	}

}
