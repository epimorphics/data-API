package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

public class Modifiers {

	public static enum Position { Inner, Outer };
	
	final Integer limit;
	final Integer offset;
	final List<Sort> sortBy;
	final Position position;
	
	public Modifiers(Position position, Integer limit, Integer offset, List<Sort> sortBy) {
		this.limit = limit;
		this.offset = offset;
		this.sortBy = sortBy == null ? new ArrayList<Sort>() : sortBy;	
		this.position = position;
	}
	
	public static Modifiers create(Position p, Integer limit, Integer offset, List<Sort> sortBy) {
		return new Modifiers(p, limit, offset, sortBy);
	}
	
	public static Modifiers trivial() {
		return new Modifiers(Position.Inner, null, null, new ArrayList<Sort>());
	}
	
	public static Modifiers sortBy(List<Sort> sortBy) {
		return new Modifiers(Position.Inner, null, null, sortBy);
	}

	public void toSparqlString(Modifiers.Position position, StringBuilder sb) {
		if (position == this.position) {
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
	}

	public static Modifiers sliceSortBy(Slice slice, List<Sort> sortby) {
		return new Modifiers(Position.Inner, slice.length, slice.offset, sortby);
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

	public boolean isInnerModifiers() {
		return position == Position.Inner;
	}

}
