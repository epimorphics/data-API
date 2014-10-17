/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.epimorphics.data_api.Switches;
import com.epimorphics.data_api.data_queries.terms.Term;
import com.epimorphics.data_api.data_queries.terms.TermArray;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;

/**
    A Row implements a row of a result-set as a map from
    element names to Values.
*/
public class Row implements JSONWritable {

	Map<String, Term> members = new HashMap<String, Term>();
	
	@Override public void writeTo(JSFullWriter jw) {
		jw.startObject();
		Collection<Entry<String, Term>> entries = getPossiblySortedEntries();
		for (Map.Entry<String, Term> e: entries) {
			Term v = e.getValue();
			if (v == null) {  jw.key(e.getKey()); jw.startArray(); jw.finishArray(); } else { v.writeMember(e.getKey(), jw); }
		}
		jw.finishObject();			
	}

	private static final Comparator<? super Entry<String, Term>> compare = new Comparator<Entry<String, Term>>() {

		@Override public int compare(Entry<String, Term> a, Entry<String, Term> b) {
			return a.getKey().compareTo(b.getKey());
		}
	};

	private Collection<Entry<String, Term>> getPossiblySortedEntries() {
		Collection<Entry<String, Term>> entries = members.entrySet();
		if (Switches.sorting) {
			List<Entry<String, Term>> es = new ArrayList<Entry<String, Term>>( entries );
			Collections.sort(es, compare);
			return es;
		} else {
			return entries;
		}
	}
	
	public Row put(String key, Term value) {
		members.put(key, value);
		return this;
	}

	public void squeezeOptionalProperty(String key) {
		Term opt = members.get(key);
		if (opt instanceof TermArray) {
			TermArray ta = (TermArray) opt;
			if (ta.size() > 0) {
				members.put(key, ta.get(0));
			} else {
				members.remove(key);
			}
		} else {
			throw new RuntimeException("BROKEN: squeezing a member that is not an array: " + opt);
		}
	}
	
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[[");
		for (Map.Entry<String, Term> e: members.entrySet()) {
			sb.append( " " ).append(e.getKey()).append(": ").append(e.getValue());
		}
		sb.append(" ]]");
		return sb.toString();			
	}
	
	@Override public boolean equals(Object other) {
		return other instanceof Row && same((Row) other);
	}

	private boolean same(Row other) {
		return members.equals(other.members);
	}
	
}