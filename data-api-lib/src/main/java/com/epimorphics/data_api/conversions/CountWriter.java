/******************************************************************
 * File:        CountWriter.java
 * Created by:  Dave Reynolds
 * Created on:  2 Apr 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.conversions;

import com.epimorphics.appbase.data.ClosableResultSet;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;

public class CountWriter implements JSONWritable {
    private final ResultSet rs;

    public CountWriter(ResultSet rs) {
        this.rs = rs;
    }
    
    @Override
    public void writeTo(final JSFullWriter out) {
        try {
            out.startArray();
            if (rs.hasNext()) {
                Literal count = rs.next().getLiteral("_count");
                if (count != null) {
                    out.startObject();
                    out.pair("@count", (Number)count.getValue());
                    out.finishObject();
                }
            }
            out.finishArray();
        } finally {
            if (rs instanceof ClosableResultSet) {
                ((ClosableResultSet)rs).close();
            }
        }

    }

    
}
