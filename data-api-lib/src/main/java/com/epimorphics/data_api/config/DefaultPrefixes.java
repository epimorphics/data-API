/******************************************************************
 * File:        DefaultPrefixes.java
 * Created by:  Dave Reynolds
 * Created on:  31 Jan 2014
 * 
 * (c) Copyright 2014, Epimorphics Limited
 *
 *****************************************************************/

package com.epimorphics.data_api.config;

import com.epimorphics.vocabs.Cube;
import com.epimorphics.vocabs.Dsapi;
import com.epimorphics.vocabs.SKOS;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class DefaultPrefixes {
    final static PrefixMapping pm = PrefixMapping.Factory.create()
            .setNsPrefix("rdf",   RDF.getURI())
            .setNsPrefix("rdfs",  RDFS.getURI())
            .setNsPrefix("owl",   OWL.getURI())
            .setNsPrefix("xsd",   XSD.getURI())
            .setNsPrefix("dct",   DCTerms.getURI())
            .setNsPrefix("skos",  SKOS.getURI())
            .setNsPrefix("qb",    Cube.getURI())
            .setNsPrefix("dsapi", Dsapi.getURI())
            .setNsPrefix("text",  "http://jena.apache.org/text#" );
            ;

    public static PrefixMapping get() {
        return pm;
    }
    
}
