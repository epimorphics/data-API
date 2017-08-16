/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.Switches;
import com.epimorphics.data_api.Version;
import com.epimorphics.data_api.conversions.Compactions;
import com.epimorphics.data_api.conversions.CountWriter;
import com.epimorphics.data_api.conversions.RowWriter;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.query.ResultSet;

public class DataQuery implements Compactions {
	
	final List<Guard> guards; 
	
	final Modifiers itemModifiers;
	final Modifiers queryModifiers;
	
	boolean suppressTypes = false;
	boolean compactOptionals = false;
	
	private final Constraint c;
	private final boolean isCount;
		
	public DataQuery(Constraint c) {
		this(c, new ArrayList<Sort>() );
	}

	public DataQuery(Constraint c, List<Sort> sortby ) {
        this(false, c, new ArrayList<Guard>(), Modifiers.sortBy(sortby), Modifiers.trivial());
    }

    public DataQuery(Constraint c, List<Sort> sortby, List<Guard> guards ) {
        this(false, c, guards, Modifiers.sortBy(sortby), Modifiers.trivial());
    }
    
    public DataQuery(Constraint c, List<Sort> sortby, Slice slice) {
        this(false, c, new ArrayList<Guard>(), Modifiers.sliceSortBy(slice, sortby), Modifiers.trivial());
    }    
    
    public DataQuery(boolean isCount, Constraint c, List<Guard> guards, Modifiers queryModifiers, Modifiers itemModifiers) {
		this.c = c;
		this.guards = guards == null ? new ArrayList<Guard>(0) : guards;
		this.itemModifiers = itemModifiers;
		this.queryModifiers = queryModifiers;
		this.isCount = isCount;
	}

	public List<Sort> sorts() {
		return queryModifiers.sortBy;
	}
	
	public String lang() {
		return null;
	}
	
	public Slice slice() {
		return new Slice(queryModifiers.limit, queryModifiers.offset, isCount);
	}
	
	public Constraint constraint() {
		return c;
	}
    
    public boolean isCountQuery() {
        return isCount;
    }

	public List<SearchSpec> getSearchPatterns() {
		// TODO revise so it's not hackery
		// this getter is only used for tests
		// so temporarily faking it out is OK
		List<SearchSpec> result = new ArrayList<SearchSpec>();
		hackery(result, c);
		return result;
	}
	
	private void hackery(List<SearchSpec> result, Constraint c) {
		if (c instanceof SearchSpec) {
			result.add(((SearchSpec) c));
		} else if (c instanceof Bool) {
			for (Constraint x: ((Bool) c).operands) hackery(result, x);
		}
	}

	public List<Constraint> filters() {
		ArrayList<Constraint> result = new ArrayList<Constraint>();
		if (c instanceof And) 
			for (Constraint cc: ((And) c).operands) {
				if (cc instanceof Filter) result.add( ((Filter) cc) );
				if (cc instanceof Below) result.add( ((Below) cc) );
			}
		if (c instanceof Filter) result.add( ((Filter) c) );
		if (c instanceof Below) result.add( ((Below) c) );
		return result;
	}

	public static final String DSAPI_Info = 
		"DSAPI " 
		+ Version.string 
		+ Version.tagname
		+ (Switches.reportSettings())
		;
	
	public static final String DSAPI_Header = "# " + DSAPI_Info + "\n";
	
    public String toSparql(Problems p, API_Dataset api) {    	
    	try {
			SQ sq = new SQ();
			StringBuilder out = new StringBuilder();
			Context rx = new Context( sq, out, this, p, api );			
			c.translate(p, rx);
			sq.setQueryModifiers(queryModifiers);
			
			String unprefixedQuery = sq.toString();
			
			if (isCountQuery()) {
				
				unprefixedQuery =
					"SELECT (COUNT (?item) AS ?_count)"
					+ "\n WHERE {"
					+ "\n{SELECT ?item {"
					+ "\n" + unprefixedQuery
					+ "\n}"
					+ "\n}"
					+ "\n}"
					;
			}
			
//			System.err.println(">> " + api.getPrefixes().getNsPrefixMap());
			String query = DSAPI_Header + PrefixUtils.expandQuery(unprefixedQuery, api.getPrefixes());
//			 System.err.println( ">> RENDERED QUERY:\n" + query );
			return query; 
		}
		catch (Exception e) { 
			p.add("exception generating SPARQL query: " + e.getMessage()); 
			e.printStackTrace(System.err); 
			return null; 
		}
    }
	    
    @Override public String toString() {
    	return 
    		c
    		+ queryModifiers.toString()
    		+ itemModifiers.toString()
    		+ (guards.isEmpty() ? "" : "\n    guards: " + guards)
    		+ "\n"
    		;
    }
    
    public JSONWritable getWriter(API_Dataset api, ResultSet resultSet) {
    	return isCountQuery()
    		? new CountWriter(resultSet)
    		: new RowWriter(api.getAspects(), resultSet, (Compactions) this)
    		;
    }

	@Override public boolean suppressTypes() {
		return suppressTypes;
	}

	@Override public boolean squeezeValues() {
		return compactOptionals;
	}

	public void setSuppressTypes(boolean suppressTypes) {
		this.suppressTypes = suppressTypes;
	}

	public void setCompactOptionals(boolean compactOptionals) {
		this.compactOptionals = compactOptionals;
	}
}
