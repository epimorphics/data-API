/******************************************************************
 * File:        LibJson.java
 * Created by:  Dave Reynolds
 * Created on:  3 Feb 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.endpoints;

import java.io.ByteArrayOutputStream;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonValue;

import com.epimorphics.appbase.core.ComponentBase;
import com.epimorphics.appbase.templates.LibPlugin;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;

/**
 * Library utilities to make it easier to work with the JSON 
 * serializations of API endpoints.
 * 
 * @author <a href="mailto:dave@epimorphics.com">Dave Reynolds</a>
 */
public class LibJson extends ComponentBase implements LibPlugin {

    public JsonValue asJson(JSONWritable js) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JSFullWriter out = new JSFullWriter(bos);
        js.writeTo(out);
        out.finishOutput();
        
        String jstring = bos.toString();
        return JSON.parseAny(jstring);
    }
}
