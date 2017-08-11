/******************************************************************
 * File:        TestUtil.java
 * Created by:  Dave Reynolds
 * Created on:  20 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.rdfutil.QueryUtil;
import com.epimorphics.util.EpiException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;

public class TestUtil {

    public static List<Resource> queryAsResultList(API_Dataset dataset, String json) {
        return QueryUtil.resultsFor(queryAsResultSet(dataset, json), "item");
    }

    public static ResultSet queryAsResultSet(API_Dataset dataset, String json) {
        return new QueryPrep(dataset, json).getResultSet();
    }
    
    public static String queryAsSparqlString(API_Dataset dataset, String json) {
        return new QueryPrep(dataset, json).getSparqlQuery();
    }

    public static String queryAsJsonString(API_Dataset dataset, String json) {
        return new QueryPrep(dataset, json).getResultString();
    }

    public static JsonValue queryAsJsonValue(API_Dataset dataset, String json) {
        return new QueryPrep(dataset, json).getResultJson();
    }

    public static JsonArray queryAsJsonArray(API_Dataset dataset, String json) {
        JsonValue jv = new QueryPrep(dataset, json).getResultJson();
        if (jv.isArray()) {
            return jv.getAsArray();
        } else {
            throw new EpiException("Expected array of results");
        }
    }
    
    final static class QueryPrep {
        String sq;
        DataQuery dq;
        API_Dataset dataset;
        Problems p = new Problems();
        ResultSet results;
        
        public QueryPrep(API_Dataset dataset, String json) {
            this.dataset = dataset;

            JsonObject query = JSON.parse(json);
            dq = DataQueryParser.Do(p, dataset, query);
            
            if (p.isOK()) {
                sq = dq.toSparql(p, dataset);
//                System.out.println(">> json:   " + json);
//                System.out.println(">> sparql: " + sq);
                try {
                    QueryFactory.create(sq);
                } catch (Exception e) {
                    p.add("Bad generated SPARQL:\n" + sq + "\n" + e.getMessage());
                }
            }
            if (!p.isOK()) {
                throw new EpiException("Failed to parse query: " + p.getProblemStrings());
            }
        }
        
        public ResultSet getResultSet() {
            if (results == null) {
                results = dataset.getSource().select(sq);
            }
            return results;
        }
        
        public String getSparqlQuery() {
            return sq;
        }
        
        public String getResultString() {
            JSONWritable jw = dq.getWriter(dataset, getResultSet());
            
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            JSFullWriter jsw = new JSFullWriter(out);
            jsw.startOutput();
            jw.writeTo(jsw);
            jsw.finishOutput();
            try {
                out.close();
            } catch (IOException e) {  /* ignore */  }
            
            try {
                return out.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {  
                throw new EpiException(e);
            }            
        }
        
        public JsonValue getResultJson() {
            return JSON.parseAny( getResultString() );
        }
    }
}
