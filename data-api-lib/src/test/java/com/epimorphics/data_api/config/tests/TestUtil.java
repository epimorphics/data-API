/******************************************************************
 * File:        TestUtil.java
 * Created by:  Dave Reynolds
 * Created on:  20 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config.tests;

import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;

import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.datasets.API_Dataset;
import com.epimorphics.data_api.reporting.Problems;
import com.epimorphics.rdfutil.QueryUtil;
import com.epimorphics.util.EpiException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestUtil {

    public static List<Resource> query(API_Dataset dataset, String json) {
        JsonObject query = JSON.parse(json);
        Problems p = new Problems();
        DataQuery q = DataQueryParser.Do(p, dataset, query);
        String sq = null;
        if (p.isOK()) {
            sq = q.toSparql(p, dataset);
//            System.out.println(sq);
        }
        if (p.isOK()) {
            ResultSet results = dataset.getManager().getSource().select(sq);
            return QueryUtil.resultsFor(results, "item");
        } else {
            throw new EpiException("Failed to parse query: " + p.getProblemStrings());
        }
        
    }
}
