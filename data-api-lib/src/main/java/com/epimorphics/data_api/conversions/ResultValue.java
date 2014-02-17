/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.conversions;

import java.util.List;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

public abstract class ResultValue implements JSONWritable {
	
	public static ResultValue fromNode(Node n) {
		if (n.isURI()) 
			return ResultValue.URI(n.getURI());
		if (n.isLiteral()) {
			String spelling = n.getLiteralLexicalForm();
			String type = n.getLiteralDatatypeURI();
			if (type == null) {
				String language = n.getLiteralLanguage();
				if (language.equals("")) {
					return ResultValue.string(spelling);
				} else {
					return ResultValue.languaged(spelling, language);
				}
			} else if (type.equals(XSDDatatype.XSDboolean.getURI())) {
				return ResultValue.bool(spelling);
			} else if (type.equals(XSDDatatype.XSDinteger.getURI())) {
				return ResultValue.integer(spelling);
			} else if (type.equals(XSDDatatype.XSDdecimal.getURI())) {
				return ResultValue.decimal(spelling);				
			} else if (type.equals(XSDDatatype.XSDfloat.getURI())) {
				return ResultValue.Double(spelling);				
			} else if (type.equals(XSDDatatype.XSDdouble.getURI())) {
				return ResultValue.Double(spelling);
			} else if (type.equals(XSDDatatype.XSDint.getURI())) {
				return ResultValue.integer(spelling);
			} else {
				return ResultValue.typed(spelling, type);
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
	
	public static abstract class Base_Value extends ResultValue {
		
		final String spelling;
		
		public Base_Value(String spelling) {
			this.spelling = spelling;
		}
		
		public boolean alike(Base_Value other) {
			return spelling.equals(other.spelling);
		}		
	}
	
	public static abstract class Primitive_Value extends Base_Value {

		public Primitive_Value(String spelling) {
			super(spelling);
		}

		@Override public void writeTo(JSFullWriter jw) {
			throw new UnsupportedOperationException();
		}

		public abstract void writeMember(String key, JSFullWriter jw);
		
		public abstract void writeElement(JSFullWriter jw);
	}
	
	public static ResultValue array(List<ResultValue> values) {
		return new Array_Value(values);
	}
	
	public static ResultValue URI(String uri) {
		return new URI_Value(uri);
	}
	
	public static ResultValue string(String spelling) {
		return new String_Value(spelling);
	}
	
	public static ResultValue languaged(String spelling, String language) {
		return new Literal_Value(spelling, "@lang", language);
	}

	public static ResultValue typed(String spelling, String type) {
		return new Literal_Value(spelling, "@type", type);
	}	
	
	public static ResultValue bool(String spelling) {
		return new Boolean_Value(spelling.equals("true"));
	}
	
	public static ResultValue integer(String spelling) {
		return new Integer_Value(spelling);
	}
	
	public static ResultValue decimal(String spelling) {
		return new Decimal_Value(spelling);
	}
	
	public static ResultValue Double(String spelling) {
		return new Double_Value(spelling);
	}	
}