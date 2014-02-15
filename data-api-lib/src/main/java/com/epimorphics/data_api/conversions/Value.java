/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.conversions;

import java.math.BigDecimal;

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
	
	final String key;
	final String spelling;
	
	public Value(String key, String spelling) {
		this.key = key;
		this.spelling = spelling;
	}
	
	public boolean alike(Value other) {
		return key.equals(other.key) && spelling.equals(other.spelling);
	}
	
	@Override public abstract void writeTo(JSFullWriter jw);

	public static Value URI(String key, String uri) {
		return new URI_Value(key, uri);
	}
	
	static class URI_Value extends Value {
		
		public URI_Value(String key, String spelling) {
			super(key, spelling);
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.key(key);
			jw.startObject();
			jw.pair("@id", spelling);
			jw.finishObject();
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof URI_Value && alike( (Value) other );
		}
	}
	
	public static Value string(String key, String spelling) {
		return new String_Value(key, spelling);
	}
	
	static class String_Value extends Value {
		
		public String_Value(String key, String spelling) {
			super(key, spelling);
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.pair(key, spelling);
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof String_Value && alike( (Value) other );
		}
	}
	
	public static Value languaged(String key, String spelling, String language) {
		return new Literal_Value(key, spelling, "@lang", language);
	}

	public static Value typed(String key, String spelling, String type) {
		return new Literal_Value(key, spelling, "@type", type);
	}	
	
	static class Literal_Value extends Value {
		
		final String subKey;
		final String subValue;
		
		public Literal_Value(String key, String spelling, String subKey, String subValue) {
			super(key, spelling);
			this.subKey = subKey;
			this.subValue = subValue;
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.key(key);
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
	
	static class Boolean_Value extends Value {
		
		final boolean value;
		
		public Boolean_Value(String key, boolean value) {
			super(key, null);
			this.value = value;
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.pair(key, value);
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof Boolean_Value && alike( (Boolean_Value) other );
		}
	}		

	public static Value integer(String key, String spelling) {
		return new Integer_Value(key, spelling);
	}
	
	static class Integer_Value extends Value {
		
		public Integer_Value(String key, String spelling) {
			super(key, spelling);
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.pair(key, Integer.parseInt(spelling));
		}	
		
		@Override public boolean equals(Object other) {
			return other instanceof URI_Value && alike( (URI_Value) other );
		}	
	}		
	
	public static Value decimal(String key, String spelling) {
		return new Decimal_Value(key, spelling);
	}
	
	static class Decimal_Value extends Value {
		
		public Decimal_Value(String key, String spelling) {
			super(key, spelling);
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.pair(key, new BigDecimal(spelling));
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof URI_Value && alike( (Decimal_Value) other );
		}
	}
	
	public static Value Double(String key, String spelling) {
		return new Double_Value(key, spelling);
	}
	
	static class Double_Value extends Value {
		
		public Double_Value(String key, String spelling) {
			super(key, spelling);
		}

		@Override public void writeTo(JSFullWriter jw) {
			jw.pair(key, Double.parseDouble(spelling));
		}
		
		@Override public boolean equals(Object other) {
			return other instanceof URI_Value && alike( (Double_Value) other );
		}
	}	
}