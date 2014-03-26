/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.data_queries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.epimorphics.data_api.aspects.Aspect;
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
	public static final Operator NOT_MATCHES = new FunctionOperator("not-matches", "contains", true);

	public static final Operator ONEOF = new OneofOperator(false);
	public static final Operator NOT_ONEOF = new OneofOperator(true);
	
	public static final Operator BELOW = new TrickyOperator("below", false);
	public static final Operator SEARCH = new TrickyOperator("search", false);
	public static final Operator NOT_BELOW = new TrickyOperator("not-below", true);
	public static final Operator NOT_SEARCH = new TrickyOperator("not-search", true);
	
	protected static final Map<String, Operator> operators = new HashMap<String, Operator>();

	static void declare_negative_pair( Operator A, Operator B ) {
		operators.put(A.JSONname(), A);
		operators.put(B.JSONname(), B);
		A.negated = B;
		B.negated = A;
	}
	
	static {
		declare_negative_pair( EQ, NE );
		declare_negative_pair( LE, GT );
		declare_negative_pair( LT, GE );
		declare_negative_pair( CONTAINS, NOT_CONTAINS );
		declare_negative_pair( MATCHES, NOT_MATCHES );
		declare_negative_pair( ONEOF, NOT_ONEOF );
		declare_negative_pair( BELOW, NOT_BELOW );
		declare_negative_pair( SEARCH, NOT_SEARCH );
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
		return operators.get(opName);
	}

	public abstract void asSparqlFilter
		( PrefixMapping pm
		, Filter newParam
		, StringBuilder sb
		, String FILTER
		, API_Dataset api
		, List<Aspect> ordered, String fVar, String value);
	
	static class InfixOperator extends Operator {
		
		final String sparqlOp;
		
		public InfixOperator(String name, String sparqlOp) {
			super(name);
			this.sparqlOp = sparqlOp;
		}

		public void asSparqlFilter
			( PrefixMapping pm
			, Filter filter
			, StringBuilder sb
			, String FILTER
			, API_Dataset api
			, List<Aspect> ordered
			, String fVar
			, String value) {
			sb.append(" ")
				.append(FILTER)
				.append("(" )
				.append(fVar)
				.append(" ")
				.append(sparqlOp)
				.append(" ")
				.append(value)
				.append(")")
				;	
		}
	}
	
	static class FunctionOperator extends Operator {
		
		final boolean negated;
		final String functionName;
		
		public FunctionOperator(String name, String functionName, boolean negated) {
			super(name);
			this.negated = negated;
			this.functionName = functionName;
		}

		public void asSparqlFilter
			( PrefixMapping pm
			, Filter filter
			, StringBuilder sb
			, String FILTER
			, API_Dataset api
			, List<Aspect> ordered, String fVar, String value) {		
			sb.append(" ")
				.append(FILTER)
				.append( "(")
				.append( negated ? "!" : "")
				.append( functionName )
				.append("(")
				.append(fVar)
				.append(", ")
				.append(value)
				.append(")")
				.append(")")
				;
		}
	}
	
	static class OneofOperator extends Operator {
		
		final boolean negated;
		
		public OneofOperator(boolean negated) {
			super(negated ? "not-oneof" : "oneof");
			this.negated = negated;
		}

		public void asSparqlFilter
			( PrefixMapping pm
			, Filter filter
			, StringBuilder sb
			, String FILTER
			, API_Dataset api
			, List<Aspect> ordered, String fVar, String value
			) {
			String orOp = "";
			List<Term> operands = filter.range.operands;
			sb.append(" ")
				.append(FILTER)
				.append("(");
			for (Term v: operands) {
				sb.append(orOp).append(fVar).append( " = ").append(v.asSparqlTerm(pm));
				orOp = " || ";
			}
			sb.append(")");
		
			
		}
	}
	
	static class TrickyOperator extends Operator {
		
		final boolean negated;
		
		public TrickyOperator(String name, boolean negated) {
			super(negated ? "not-oneof" : "oneof");
			this.negated = negated;
		}

		public void asSparqlFilter
			( PrefixMapping pm
			, Filter filter
			, StringBuilder sb
			, String FILTER
			, API_Dataset api
			, List<Aspect> ordered, String fVar, String value
			) {
			sb.append(">> ").append(JSONname).append("not implemented yet");
		}
	}
	
	static class EqOperator extends Operator {
		
		final String opName;
		
		public EqOperator(String name, String op) {
			super(name);
			this.opName = "=";
			
		}

		public void asSparqlFilter
			( PrefixMapping pm
			, Filter filter
			, StringBuilder sb
			, String FILTER
			, API_Dataset api
			, List<Aspect> ordered, String fVar, String value
			) {
		//		sb.append("?item ").append(f.name.prefixed).append( " ").append(value);
		//		sb.append(" BIND(").append(value).append(" AS ").append(fVar).append(")");
			sb.append(" ")
				.append(FILTER)
				.append("(" )
				.append(fVar)
				.append(" ")
				.append("=")
				.append(" ")
				.append(value)
				.append(")")
				;
		}
	}
	
}
