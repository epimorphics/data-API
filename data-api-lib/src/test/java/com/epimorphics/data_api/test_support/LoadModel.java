/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.test_support;

import java.io.StringReader;

import com.epimorphics.vocabs.Dsapi;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.XSD;

/**
    Utility class for loading a Turtle model from a string
    in the context of a bunch of useful prefixes.

*/
public class LoadModel {

	/**
		The prefixes used by default by modelFromTurtle.
	*/
	public static final String PREFIXES = 
	    "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n"
	    + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
	    + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
	    + "@prefix xsd: <" + XSD.getURI() + "> .\n"
	    + "@prefix : <http://www.epimorphics.com/tools/example#> .\n"
	    + "@prefix dsapi: <" + Dsapi.NS + "> ."
	    ;

	/**
	    Create a model by reading the Turtle string ttl, using the prefixes
	    from PREFIXES.
	*/
	public static Model modelFromTurtle(String ttl) {
		Model model = ModelFactory.createDefaultModel();
		model.read( new StringReader(PREFIXES + ttl), null, "Turtle");
		return model;
	}

}
