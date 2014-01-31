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
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class DefaultPrefixes {
    final static PrefixMapping pm = PrefixMapping.Factory.create()
            .setNsPrefix("rdf",   RDF.getURI())
            .setNsPrefix("rdfs",  RDFS.getURI())
            .setNsPrefix("owl",   OWL.getURI())
            .setNsPrefix("xsd",   XSD.getURI())
            .setNsPrefix("dct",   DCTerms.getURI())
            .setNsPrefix("skos",  SKOS.getURI())
            .setNsPrefix("qb",    Cube.getURI() + "#")
            .setNsPrefix("dsapi", Dsapi.getURI())
            ;

    public static PrefixMapping get() {
        return pm;
    }
    
}
