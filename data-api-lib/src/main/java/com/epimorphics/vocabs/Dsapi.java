/* CVS $Id: $ */
package com.epimorphics.vocabs; 
import com.hp.hpl.jena.rdf.model.*;
 
/**
 * Vocabulary definitions from src/main/vocabs/dsapi.ttl 
 * @author Auto-generated by schemagen on 28 Jan 2014 12:10 
 */
public class Dsapi {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://www.epimorphics.com/public/vocabulary/dsapi#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    /** <p>Indicates a locally-configured 'aspect' of the data set.</p> */
    public static final Property aspect = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#aspect" );
    
    /** <p>Gives the textual source of a SPARQL BGP which will bind any member of the 
     *  dataset to the ?item variable.</p>
     */
    public static final Property baseQuery = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#baseQuery" );
    
    /** <p>Lowest value expected for a measure or other cube component</p> */
    public static final Property lowerBound = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#lowerBound" );
    
    /** <p>Set to true if the aspect can have multiple values, default is false</p> */
    public static final Property multivalued = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#multivalued" );
    
    /** <p>Set to true if the aspect is optional, default is false</p> */
    public static final Property optional = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#optional" );
    
    /** <p>Source text of a SPARQL property path expression that links a element of the 
     *  data set to the aspect value.</p>
     */
    public static final Property propertyPath = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#propertyPath" );
    
    /** <p>Indicates a Data Cube dataset whose observations are the contents of this 
     *  dsapi data set</p>
     */
    public static final Property qb_dataset = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#qb_dataset" );
    
    /** <p>Indicates a Data Cube DataStructureDefinition defining the aspect structure 
     *  of the data set.</p>
     */
    public static final Property qb_dsd = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#qb_dsd" );
    
    /** <p>Indicates limits to the range of values which will be present for this aspect.</p> */
    public static final Property rangeConstraint = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#rangeConstraint" );
    
    /** <p>Highest value expected for a measure or other cube component</p> */
    public static final Property upperBound = m_model.createProperty( "http://www.epimorphics.com/public/vocabulary/dsapi#upperBound" );
    
    /** <p>Specification of a single aspect of the data set</p> */
    public static final Resource Aspect = m_model.createResource( "http://www.epimorphics.com/public/vocabulary/dsapi#Aspect" );
    
    /** <p>Specification for a data set to accessed through the data services API. Needs 
     *  either a qb_dataset or a baseQuery to define the contents of the dataset. 
     *  The structure is taken from either the :aspect definitions given here, from 
     *  directly reference DSD or implicitly from the DSD associated with the QB dataset.</p>
     */
    public static final Resource Dataset = m_model.createResource( "http://www.epimorphics.com/public/vocabulary/dsapi#Dataset" );
    
    /** <p>Constraint on the range of values which will be present for the corresponding 
     *  aspect.</p>
     */
    public static final Resource RangeConstraint = m_model.createResource( "http://www.epimorphics.com/public/vocabulary/dsapi#RangeConstraint" );
    
}