/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/
package com.epimorphics.data_api.datasets;

import static com.epimorphics.data_api.config.JSONConstants.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epimorphics.appbase.data.SparqlSource;
import com.epimorphics.appbase.monitor.ConfigInstance;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.config.DSAPIManager;
import com.epimorphics.data_api.config.Hierarchy;
import com.epimorphics.data_api.config.ResourceBasedConfig;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.json.JSFullWriter;
import com.epimorphics.json.JSONWritable;
import com.epimorphics.rdfutil.RDFUtil;
import com.epimorphics.vocabs.Cube;
import com.epimorphics.vocabs.Dsapi;
import com.epimorphics.vocabs.SKOS;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.XSD;

public class API_Dataset extends ResourceBasedConfig implements ConfigInstance {

	static final Logger log = LoggerFactory.getLogger(API_Dataset.class);
    
    String name;
	String query;
	DSAPIManager manager;
	String sourceName;
	Hierarchy hierarchy;
	
	final Set<Resource> literalTypes = new HashSet<Resource>();
	
	final Map<Shortname, Aspect> nameToAspect = new HashMap<Shortname, Aspect>();

	private static final String XSD_URI = XSD.getURI();
	
	public API_Dataset(String name) {
		this.name = name;
	}
	
	public API_Dataset() {
	}
	
	public API_Dataset(Resource config, DSAPIManager manager) {
	    super(config);
	    configureLiteralDatatypes(config);
	    configureHierarchy();
	    configureBaseQuery();
	    configureName();
	    this.manager = manager;
	}
	
	static final Property DSAPI_LiteralType = ResourceFactory.createProperty(Dsapi.NS + "literalType");
	
	/**
	    configureLiteralDatatypes(config) considers all the properties (dsapi:literalType T) of
	    config that are not literals and adds them to the set literalTypes of known literal types.
	*/
	private void configureLiteralDatatypes(Resource config) {
		for (RDFNode r: config.listProperties(DSAPI_LiteralType).mapWith(Statement.Util.getObject).toList()) {
			if (r instanceof Resource) {
				setIsLiteralType((Resource) r);				
			}
		}
	}
	
	/**
	    Add type to the set of known literal types.
	*/
	public void setIsLiteralType(Resource type) {
		literalTypes.add(type);
	}

	private void configureHierarchy() {
	    if (root.hasProperty(Dsapi.codeList)) {
	        hierarchy = new Hierarchy( getResourceValue(Dsapi.codeList) );
	    }
	}
	
    private void configureBaseQuery() {
        query = getStringValue(Dsapi.baseQuery, null);
        if (query == null) {
            Resource dataset = getResourceValue(Dsapi.qb_dataset);
            if (dataset != null) {
                query = "?item  <" + Cube.dataSet + "> <" + getResourceValue(Dsapi.qb_dataset).getURI() + "> .";
            } else if (isHierarchy()) {
                query = hierarchy.getMemberQuery("item");
            } else {
                query = "";
            }
        }
    }
    
    private void configureName() {
        name = RDFUtil.getStringValue(root, SKOS.notation, RDFUtil.getLocalname(root));
    }
    
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	    isLiteralType(type) returns true if type is known to be
	    a literal type, ie it is either an XSD type or it has
	    been declared using configureLiteralDatatypes or
	    setIsLiteralType.
	    
	    A type is "literal" if values of that type are typed
	    literals.
	*/
	public boolean isLiteralType(Resource type) {
		if (type == null) return false;
		String tu = type.getURI();
		return tu.startsWith(XSD_URI) || literalTypes.contains(type);		
	}

	public Set<Aspect> getAspects() {
		return new HashSet<Aspect>( nameToAspect.values() );
	}

	/**
	    Add an aspect 'a' to this dataset. Return this
	    dataset for chaining.
	*/
	public API_Dataset add(Aspect a) {
		nameToAspect.put(a.getName(), a);
		return this;
	}
	
	public Aspect getAspectNamed(Shortname name) {
		return nameToAspect.get(name);		
	}
	
	public boolean isHierarchy() {
	    return hierarchy != null;
	}
	
	public Hierarchy getHierarchy() {
	    return hierarchy;
	}
	
	public DSAPIManager getManager() {
	    return manager;
	}
	
	// From base class have:
	// getLabel()       getLabel(lang)
	// getDescription() getDescription(lang)
	
	/**
	 * Return the SPARQL pattern which finds entries in the data set and binds them to ?item.
	 */
	public String getBaseQuery() {
	    return query;
	}
	
	/**
	    Setter (mostly for testing)
	*/
	public API_Dataset setBaseQuery(String baseQuery) {
		this.query = baseQuery;
		return this;
	}
	
	public void setSourceName(String sourceName) {
	    this.sourceName = sourceName;
	}
	
	/**
	 * Return the sparql source which should be used to 
	 * query this dataset
	 */
	public SparqlSource getSource() {
	    return manager.getSource(sourceName);
	}
    
    /**
     * Full json serialization used to report the dataset structure, language specific
     */
    public JSONWritable asJson(String lang, String uribase) {
        return new Writer(lang, uribase);
    }
    
    /**
     * Shortfurm summary json serialization
     */
    public JSONWritable asJsonShort(String lang, String uribase) {
        return new ShortWriter(lang, uribase);
    }

    /**
     * Short form json serialization using the dataset list endpoint, language specific
     */
    public void writeShortTo(JSFullWriter out, String lang, String uribase) {
        out.startObject();
        writeSummary(out, lang, uribase);
        out.finishObject();
    }
    
    private void writeSummary(JSFullWriter out, String lang, String uribase) {
        out.pair(ID, root.getURI());
        out.pair(NAME, getName());
        out.pair(LABEL, getLabel(lang));
        out.pair(DESCRIPTION, getDescription(lang));
        if (manager != null) {
            String base = uribase + name;
            out.pair(DATA_API, base + "/data");
            out.pair(STRUCTURE_API, base + "/structure");
            out.pair(DESCRIBE_API, base + "/describe");
        }
        if (sourceName != null) {
            out.pair(SOURCE, sourceName);
        }
        if (isHierarchy()) {
            out.key(HIERARCHY);
            out.startObject();
            out.pair(ID, hierarchy.getRoot().getURI());
            safeOut(out, LABEL, hierarchy.getLabel(lang));
            safeOut(out, DESCRIPTION, hierarchy.getDescription(lang));
            out.pair(TYPE, hierarchy.getType().getURI());
            out.finishObject();
        }
    }
    
    private void safeOut(JSFullWriter out, String key, String value) {
        if (value != null) {
            out.pair(key, value);
        }
    }

    public void writeJson(JSFullWriter out, String lang, String uribase) {
        out.startObject();
        writeSummary(out, lang, uribase);
        Resource dsd = root.getPropertyResourceValue(Dsapi.qb_dsd);
        if (dsd != null) {
            out.pair(DSD, dsd.getURI());
        }
        out.key(ASPECTS);
        out.startArray();
        for (Iterator<Aspect> ai = getAspects().iterator(); ai.hasNext();) {
            ai.next().writeJson(out, lang);
            if (ai.hasNext()) {
                out.arraySep();
            }
        }
        out.finishArray();
        out.finishObject();
    }
    
    class Writer implements JSONWritable {
        String lang;
        String uribase;
        public Writer(String lang, String uribase) {  
        	this.lang = lang; this.uribase = uribase; 
        }
        
        @Override public void writeTo(JSFullWriter out) {
            writeJson(out, lang, uribase);
        }
    }
    
    class ShortWriter implements JSONWritable {
        String lang;
        String uribase;
        public ShortWriter(String lang, String uribase) {  
        	this.lang = lang; this.uribase = uribase; 
        }
        
        @Override public void writeTo(JSFullWriter out) {
            writeShortTo(out, lang, uribase);
        }
    }

}