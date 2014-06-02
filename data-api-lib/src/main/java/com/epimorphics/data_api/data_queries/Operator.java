/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.hp.hpl.jena.shared.PrefixMapping;

public abstract class Operator {
	
	protected String JSONname;
	protected String infixName;
	protected Operator negated;

	public static final Operator EQ = new EqOperator("eq", "=");
	public static final Operator LT = new InfixOperator("lt", "<");
	public static final Operator GT = new InfixOperator("gt", ">");
	public static final Operator LE = new InfixOperator("le", "<=");
	public static final Operator NE = new InfixOperator("ne", "!=");
	public static final Operator GE = new InfixOperator("ge", ">=");

	public static final Operator MATCHES = new FunctionOperator("matches", "regex", false);
	public static final Operator CONTAINS = new FunctionOperator("contains", "contains", false);
	
	public static final Operator NOT_CONTAINS = new FunctionOperator("not-contains", "contains", true);
	public static final Operator NOT_MATCHES = new FunctionOperator("not-matches", "regex", true);

	public static final Operator ONEOF = new OneofOperator("oneof", false);
	public static final Operator NOT_ONEOF = new OneofOperator("not-oneof", true);
	
	public static final Operator BELOW = new BelowOperator("below", false);
	public static final Operator SEARCH = new SearchOperator("search", false);
	public static final Operator NOT_BELOW = new BelowOperator("not-below", true);
	public static final Operator NOT_SEARCH = new SearchOperator("not-search", true);
	
	protected static final Map<String, Operator> operators = new HashMap<String, Operator>();

	static void declare_complementary_pair( Operator A, Operator B ) {
		operators.put(A.JSONname(), A);
		operators.put(B.JSONname(), B);
		A.negated = B;
		B.negated = A;
	}
	
	static {
		declare_complementary_pair( EQ, NE );
		declare_complementary_pair( LE, GT );
		declare_complementary_pair( LT, GE );
		declare_complementary_pair( CONTAINS, NOT_CONTAINS );
		declare_complementary_pair( MATCHES, NOT_MATCHES );
		declare_complementary_pair( ONEOF, NOT_ONEOF );
		declare_complementary_pair( BELOW, NOT_BELOW );
		declare_complementary_pair( SEARCH, NOT_SEARCH );
	}
	
	public Operator(String name) {
		this.JSONname = name;
	}
	
	@Override public String toString() {
		return JSONname;
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Operator && same((Operator) other);
	}
	
	private boolean same(Operator other) {
		return this.JSONname.equals(other.JSONname);
	}
	
	@Override public int hashCode() {
		return JSONname.hashCode();
	}

	public String asInfix() {
		return infixName;
	}

	public boolean isInfix() {
		return infixName != null;
	}

	public Operator negate() {
		return negated;
	}

	public String JSONname() {
		return JSONname;
	}

	public static Operator lookup(String opName) {
		Operator result = operators.get(opName);
		return result;
	}

	public abstract void asConstraint
		( Filter newParam
		, StringBuilder sb
		, API_Dataset api
		, String varSuffix
		);
	
	static class InfixOperator extends Operator {
		
		public final String sparqlOp;
		
		public InfixOperator(String name, String sparqlOp) {
			super(name);
			this.sparqlOp = sparqlOp;
		}

		public void asConstraint
			( Filter filter
			, StringBuilder sb
			, API_Dataset api
			, String varSuffix
			) {
			String fVar = filter.a.asVar();
			PrefixMapping pm = api.getPrefixes();
			String value = filter.range.operands.get(0).asSparqlTerm(pm);
			sb.append(" ")
				.append(fVar).append(varSuffix)
				.append(" ")
				.append(sparqlOp)
				.append(" ")
				.append(value)
				;	
		}
	}
	
	static public class FunctionOperator extends Operator {
		
		final boolean needsNot;
		public final String functionName;
		
		public FunctionOperator(String name, String functionName, boolean negated) {
			super(name);
			this.needsNot = negated;
			this.functionName = functionName;
		}

		public void asConstraint
			( Filter filter
			, StringBuilder sb
			, API_Dataset api
			, String varSuffix
		) {	
			PrefixMapping pm = api.getPrefixes();
			List<Term> operands = filter.range.operands;
			String value = filter.range.operands.get(0).asSparqlTerm(pm);
			String fVar = filter.a.asVar();
			sb.append(" ")
				.append( needsNot ? "!" : "")
				.append( functionName )
				.append("(")
				.append(fVar).append(varSuffix)
				.append(", ")
				.append(value)
				.append(operands.size() == 2 ? ", " + operands.get(1).asSparqlTerm(pm) : "")
				.append(")")
				;
		}
	}
	
	static class OneofOperator extends Operator {
		
		final boolean negated;
		
		public OneofOperator(String name, boolean negated) {
			super(name);
			this.negated = negated;
		}

		public void asConstraint
			( Filter filter
			, StringBuilder sb
			, API_Dataset api
			, String varSuffix
			) {
			PrefixMapping pm = api.getPrefixes();
			String fVar = filter.a.asVar();
			String orOp = "";
			sb.append(" ");
			for (Term v: filter.range.operands) {
				sb
					.append(orOp)
					.append(fVar).append(varSuffix)
					.append( " = ")
					.append(v.asSparqlTerm(pm))
					;
				orOp = " || ";
			}
		}
	}
	
	public static class EqOperator extends Operator {
		
		final String opName;
		
		public EqOperator(String name, String op) {
			super(name);
			this.opName = "=";
		}

		public void asConstraint
			( Filter filter
			, StringBuilder sb
			, API_Dataset api
			, String varSuffix
			) {
			PrefixMapping pm = api.getPrefixes();
			String fVar = filter.a.asVar();
			String value = filter.range.operands.get(0).asSparqlTerm(pm);
			sb.append(" ")
				.append(fVar).append(varSuffix)
				.append(" ")
				.append("=")
				.append(" ")
				.append(value)
				;
		}
	}		
	
	static class BelowOperator extends Operator {
		
		final boolean negated;
		
		public BelowOperator(String name, boolean negated) {
			super(name);
			this.negated = negated;
		}

		public void asConstraint
			( Filter filter
			, StringBuilder sb
			, API_Dataset api
			, String varSuffix
			) {
			PrefixMapping pm = api.getPrefixes();
			String fVar = filter.a.asVar();
			String value = filter.range.operands.get(0).asSparqlTerm(pm);			
			Aspect x = api.getAspectNamed(filter.a.getName());
			String below = x.getBelowPredicate(api);
			sb.append(value)
				.append(" ")
				.append(below)
				.append("* ")
				.append(fVar).append(varSuffix)
				.append(" .")
				.append("\n")
				;		
		}
	}
	
	static class SearchOperator extends Operator {
		
		final boolean negated;
		
		public SearchOperator(String name, boolean negated) {
			super(name);
			this.negated = negated;
		}

		public void asConstraint
			( Filter filter
			, StringBuilder sb
			, API_Dataset api
			, String varSuffix
			) {	
			PrefixMapping pm = api.getPrefixes();
			String fVar = filter.a.getName().asVar();
			String value = filter.range.operands.get(0).asSparqlTerm(pm);
			sb
				.append(fVar).append(varSuffix)
				.append(" <http://jena.apache.org/text#query> ")
				.append(value)
				.append(" .")
				;
		}
	}
	
}
