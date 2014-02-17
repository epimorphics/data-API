/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.conversions;

import java.math.BigDecimal;
import java.util.List;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

public abstract class Value implements JSONWritable {
	
	public static Value fromNode(String key, Node n) {
		if (n.isURI()) 
			return Value.URI(key, n.getURI());
		if (n.isLiteral()) {
			String spelling = n.getLiteralLexicalForm();
			String type = n.getLiteralDatatypeURI();
			if (type == null) {
				String language = n.getLiteralLanguage();
				if (language.equals("")) {
					return Value.string(key, spelling);
				} else {
					return Value.languaged(key, spelling, language);
				}
			} else if (type.equals(XSDDatatype.XSDboolean.getURI())) {
				return Value.bool(key, spelling);
			} else if (type.equals(XSDDatatype.XSDinteger.getURI())) {
				return Value.integer(key, spelling);
			} else if (type.equals(XSDDatatype.XSDdecimal.getURI())) {
				return Value.decimal(key, spelling);				
			} else if (type.equals(XSDDatatype.XSDfloat.getURI())) {
				return Value.Double(key, spelling);				
			} else if (type.equals(XSDDatatype.XSDdouble.getURI())) {
				return Value.Double(key, spelling);
			} else if (type.equals(XSDDatatype.XSDint.getURI())) {
				return Value.integer(key, spelling);
			} else {
				return Value.typed(key, spelling, type);
			}
		}
		throw new RuntimeException("cannot handle this node: " + n);
	}

	public void writeMember(String key, JSFullWriter jw) {
		jw.key(key);
		writeTo(jw);
	}
	
	public void writeElement(JSFullWriter jw) {
		writeTo(jw);
	}
	
	static abstract class BaseValue extends Value {
		
		final String spelling;
		
		public BaseValue(String key, String spelling) {
			this.spelling = spelling;
		}
		
		public boolean alike(BaseValue other) {
			return spelling.equals(other.spelling);
		}
		
		@Override public abstract void writeTo(JSFullWriter jw);
		
	}
	
	public static Value array(List<Value> values) {
		return new Array_Value(values);
	}
	
	static class Array_Value extends Value {
		
		final List<Value> values;
		
		public Array_Value(List<Value> values) {
			this.values = values;
		}
		
		@Override public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append( "[" );
			for (Value v: values) sb.append(" ").append(v);
			sb.append( " ]" );
			return sb.toString();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof Array_Value && same( (Array_Value) other);
		}

		private boolean same(Array_Value other) {
			return values.equals(other.values);
		}

		@Override public void writeTo(JSFullWriter out) {
			out.startArray();
			for (Value v: values) v.writeTo(out);
			out.finishArray();
		}
	}
	
	public static Value URI(String key, String uri) {
		return new URI_Value(key, uri);
	}
	
	static class URI_Value extends BaseValue {
		
		public URI_Value(String key, String spelling) {
			super(key, spelling);
		}
		
		@Override public String toString() {
			return "<" + spelling + ">";
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.startObject();
			jw.pair("@id", spelling);
			jw.finishObject();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof URI_Value && alike( (URI_Value) other );
		}
	}
	
	public static Value string(String key, String spelling) {
		return new String_Value(key, spelling);
	}
	
	static abstract class Primitive_Value extends BaseValue {

		public Primitive_Value(String key, String spelling) {
			super(key, spelling);
		}

		@Override public void writeTo(JSFullWriter jw) {
			throw new UnsupportedOperationException();
		}

		public abstract void writeMember(String key, JSFullWriter jw);
		
		public abstract void writeElement(JSFullWriter jw);
	}
	
	static class String_Value extends Primitive_Value {
		
		public String_Value(String key, String spelling) {
			super(key, spelling);
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof String_Value && alike( (String_Value) other );
		}
		
		@Override public String toString() {
			return "'" + spelling + "'";
		}

		@Override public void writeMember(String key, JSFullWriter jw) {
			jw.pair(key, spelling);
		}

		@Override public void writeElement(JSFullWriter jw) {
			jw.arrayElement(spelling);
		}
	}
	
	public static Value languaged(String key, String spelling, String language) {
		return new Literal_Value(key, spelling, "@lang", language);
	}

	public static Value typed(String key, String spelling, String type) {
		return new Literal_Value(key, spelling, "@type", type);
	}	
	
	static class Literal_Value extends BaseValue {
		
		final String subKey;
		final String subValue;
		
		public Literal_Value(String key, String spelling, String subKey, String subValue) {
			super(key, spelling);
			this.subKey = subKey;
			this.subValue = subValue;
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.startObject();
			jw.pair("@value", spelling);
			jw.pair(subKey, subValue);
			jw.finishObject();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof Literal_Value && same( (Literal_Value) other );
		}

		private boolean same(Literal_Value other) {
			return 
				alike(other)
				&& subKey.equals(other.subKey) && subValue.equals(other.subValue)
				;
		}
	}
	
	public static Value bool(String key, String spelling) {
		return new Boolean_Value(key, spelling.equals("true"));
	}
	
	static class Boolean_Value extends Primitive_Value {
		
		final boolean value;
		
		public Boolean_Value(String key, boolean value) {
			super(key, null);
			this.value = value;
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof Boolean_Value && alike( (Boolean_Value) other );
		}

		@Override public void writeMember(String key, JSFullWriter jw) {
			jw.pair(key, spelling.equals("true"));
		}

		@Override public void writeElement(JSFullWriter jw) {
			jw.arrayElement(spelling.equals("true"));
		}
	}		

	public static Value integer(String key, String spelling) {
		return new Integer_Value(key, spelling);
	}
	
	static class Integer_Value extends Primitive_Value {
		
		public Integer_Value(String key, String spelling) {
			super(key, spelling);
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof Integer_Value && alike( (URI_Value) other );
		}

		@Override public void writeMember(String key, JSFullWriter jw) {
			jw.pair(key, Integer.parseInt(spelling));
		}

		@Override public void writeElement(JSFullWriter jw) {
			jw.arrayElement(Integer.parseInt(spelling));
		}	
	}		
	
	public static Value decimal(String key, String spelling) {
		return new Decimal_Value(key, spelling);
	}
	
	static class Decimal_Value extends Primitive_Value {
		
		public Decimal_Value(String key, String spelling) {
			super(key, spelling);
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof Decimal_Value && alike( (Decimal_Value) other );
		}

		@Override public void writeMember(String key, JSFullWriter jw) {
			jw.pair(key, new BigDecimal(spelling));
		}

		@Override public void writeElement(JSFullWriter jw) {
			// TODO jw.arrayElement(new BigDecimal(spelling));
			throw new UnsupportedOperationException();
		}
	}
	
	public static Value Double(String key, String spelling) {
		return new Double_Value(key, spelling);
	}
	
	static class Double_Value extends Primitive_Value {
		
		public Double_Value(String key, String spelling) {
			super(key, spelling);
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof Double_Value && alike( (Double_Value) other );
		}

		@Override public void writeMember(String key, JSFullWriter jw) {
			jw.pair(key, Double.parseDouble(spelling));
		}

		@Override public void writeElement(JSFullWriter jw) {
			// TODO jw.arrayElement(Double.parseDouble(spelling));
			throw new UnsupportedOperationException();
		}
	}	
}