/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.data_queries;

import java.util.ArrayList;
import java.util.List;

import com.epimorphics.data_api.Version;
import com.epimorphics.data_api.conversions.CountWriter;
import com.epimorphics.data_api.conversions.RowWriter;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.data_api.sparql.SQ;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.util.PrefixUtils;
import com.hp.hpl.jena.query.ResultSet;

public class DataQuery {
	
	final List<Sort> sortby;
	final Slice slice;
	final List<Guard> guards; 
	
	private final Constraint c;
	
	public DataQuery(Constraint c) {
		this(c, new ArrayList<Sort>() );
	}

	public DataQuery(Constraint c, List<Sort> sortby ) {
        this(c, sortby, null, Slice.all());
    }

    public DataQuery(Constraint c, List<Sort> sortby, List<Guard> guards ) {
        this(c, sortby, guards, Slice.all());
    }
    
    public DataQuery(Constraint c, List<Sort> sortby, Slice slice) {
        this(c, sortby, null, slice);
    }    
    
    public DataQuery(Constraint c, List<Sort> sortby, List<Guard> guards, Slice slice) {
		this.c = c;
		this.sortby = sortby;
		this.slice = slice;
		this.guards = guards == null ? new ArrayList<Guard>(0) : guards;
	}

	public List<Sort> sorts() {
		return sortby;
	}
	
	public String lang() {
		return null;
	}
	
	public Slice slice() {
		return slice;
	}
	
	public Constraint constraint() {
		return c;
	}
    
    public boolean isCountQuery() {
        return slice.isCount;
    }
    
    public boolean isNestedCountQuery() {
        return slice.isCount && (slice.length != null || slice.offset != null);
    }

	public List<SearchSpec> getSearchPatterns() {
		// TODO revise so it's not hackery
		// this getter is only used for tests
		// so tempoarilty faking it out is OK
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

	public static final String DSAPI_Info = "DSAPI " + Version.string + Version.tagname;
	public static final String DSAPI_Header = "# " + DSAPI_Info + "\n";
	
    public String toSparql(Problems p, API_Dataset api) {
    	try {
			SQ sq = new SQ();
			StringBuilder out = new StringBuilder();
			Context rx = new Context( sq, out, this, p, api );
			
			c.translate(p, rx);
			if (sortby.size() > 0) sq.comment(sortby.size() + " sort specifications");
			sq.addSorts(sortby);			
			
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
//			System.err.println( ">> RENDERED QUERY:\n" + query );
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
    		+ (sortby.isEmpty() ? "" : "\n    sortby: " + sortby)
    		+ (!slice.isAll() ? ""   : "\n    slice:  " + slice)
    		+ (guards.isEmpty() ? "" : "\n    guards: " + guards)
    		+ "\n"
    		;
    }
    
    public JSONWritable getWriter(API_Dataset api, ResultSet resultSet) {
    	return isCountQuery()
    		? new CountWriter(resultSet)
    		: new RowWriter(api.getAspects(), resultSet)
    		;
    }
}