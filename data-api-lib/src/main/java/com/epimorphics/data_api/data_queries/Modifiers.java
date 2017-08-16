package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

public class Modifiers {

	final Integer limit;
	final Integer offset;
	final List<Sort> sortBy;
	
	public Modifiers(Integer limit, Integer offset, List<Sort> sortBy) {
		this.limit = limit;
		this.offset = offset;
		this.sortBy = sortBy == null ? new ArrayList<Sort>() : sortBy;	}
	
	public static Modifiers create(Integer limit, Integer offset, List<Sort> sortBy) {
		return new Modifiers(limit, offset, sortBy);
	}
	
	public static Modifiers trivial() {
		return new Modifiers(null, null, new ArrayList<Sort>());
	}
	
	public static Modifiers sortBy(List<Sort> sortBy) {
		return new Modifiers(null, null, sortBy);
	}

	public void toSparqlString(StringBuilder sb) {
		if (sortBy.size() > 0) {
			sb.append( " ORDER BY");
			for (Sort s: sortBy) {
				sb.append(" ");
				s.toString(sb);
			}
		}
		if (limit != null) sb.append(" LIMIT " ).append(limit);
		if (offset != null) sb.append(" OFFSET " ).append(offset);
	}

	public static Modifiers sliceSortBy(Slice slice, List<Sort> sortby) {
		return new Modifiers(slice.length, slice.offset, sortby);
	}
	
	public String toString() {
		return
			(limit == null ? "" : "limit: " +limit + "\n")
			+ (offset == null ? "" : "offset: " + offset + "\n")
			+ (sortBy.isEmpty() ? "" : "sortBy: " + sortBy + "\n")
			;
	}

	public boolean isNonTrivial() {
		return limit != null || offset != null || !sortBy.isEmpty();
	}

}
