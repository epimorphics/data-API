/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.conversions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;

/**
    A Row implements a row of a result-set as a map from
    element names to Values.
*/
public class Row implements JSONWritable {

	Map<String, ResultValue> members = new HashMap<String, ResultValue>();
	
	static boolean sorting = false;
	
	@Override public void writeTo(JSFullWriter jw) {
		jw.startObject();
		Collection<Entry<String, ResultValue>> entries = getPossiblySortedEntries();
		for (Map.Entry<String, ResultValue> e: entries) {
			ResultValue v = e.getValue();
			if (v == null) {  jw.key(e.getKey()); jw.startArray(); jw.finishArray(); } else { v.writeMember(e.getKey(), jw); }
		}
		jw.finishObject();			
	}

	private static final Comparator<? super Entry<String, ResultValue>> compare = new Comparator<Entry<String, ResultValue>>() {

		@Override public int compare(Entry<String, ResultValue> a, Entry<String, ResultValue> b) {
			return a.getKey().compareTo(b.getKey());
		}
	};

	private Collection<Entry<String, ResultValue>> getPossiblySortedEntries() {
		Collection<Entry<String, ResultValue>> entries = members.entrySet();
		if (sorting) {
			List<Entry<String, ResultValue>> es = new ArrayList<Entry<String, ResultValue>>( entries );
			Collections.sort(es, compare);
			return es;
		} else {
			return entries;
		}
	}
	
	public Row put(String key, ResultValue value) {
		members.put(key, value);
		return this;
	}
	
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[[");
		for (Map.Entry<String, ResultValue> e: members.entrySet()) {
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