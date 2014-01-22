/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
*/

package com.epimorphics.data_api.endpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonString;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.AppConfig;
import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.conversions.Convert;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.data_queries.Shortname;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.Map1;

// placeholder endpoint, has a fake setup rather than a proper
// configuration.
@Path( "placeholder") public class Placeholder {
	
	public static class Config {
		String filePath;
		String name;
		
		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	@GET @Path("dataset/{name}/submit") @Produces("text/html") public Response submit
		(@PathParam("name") String name) {
		String entity = BunchLib.join(
			"<html>"
			, "<head>"
			, "<title>data API placeholder query submitter</title>"
			, "</head>"
			, "<body>"
			, "<h1>submit JSON query on " + name + " </h1>"
			, "<form method='POST' action='/data-api/placeholder/dataset/" + name + "/data'>"
			, "<textarea cols='80' rows='20' name='json'>"
			, "</textarea>"
			, "<div><input type='submit' name='button' value='SUBMIT' /></div>"
			, "</form>"
			, "</body>"
			, "</html>"
			);
		return Response.ok(entity).build();
	}
	
	static class Example {
		
		final PrefixMapping pm;
		final Aspects aspects;
		final Model model;
		
		Example(PrefixMapping pm, Aspects aspects, Model model) {
			this.pm = pm;
			this.aspects = aspects;
			this.model = model;
		}
		
	}
	
	static Example configureGames(Model m) {
		PrefixMapping pm = PrefixMapping.Factory
				.create()
				.setNsPrefixes(PrefixMapping.Extended)
				.setNsPrefixes(m)
				.lock();
		
//		String egc = "http://epimorphics.com/public/vocabulary/games.ttl#";
//		
//		Aspects aspects = new Aspects()
//			.include(new Aspect( RDF.getURI() + "type", new Shortname(pm, "rdf:type" )))
//			.include(new Aspect( RDFS.getURI() + "label", new Shortname(pm, "rdfs:label" )))
//			.include(new Aspect( egc + "players", new Shortname(pm, "egc:players" )))
//			.include(new Aspect( egc + "pubYear", new Shortname(pm, "egc:pubYear" )))
//			;		Set<Property> predicates = m.listStatements().mapWith(Statement.Util.getPredicate).toSet();
		
		Set<Property> predicates = m.listStatements().mapWith(Statement.Util.getPredicate).toSet();

		Aspects aspects = new Aspects();
		
		for (Property p: predicates) {
			Resource rangeType = findRangeType(m, p);
			String ID = p.getURI();
			String sn = pm.shortForm(ID);
			Aspect a = new Aspect(ID, new Shortname(pm, sn));
			if (rangeType != null) a.setRangeType(rangeType);
			aspects.include(a);
		}

		return new Example( pm, aspects, m );
	}
	
	static Example configureICM(Model m) {
		
		PrefixMapping pm = PrefixMapping.Factory
				.create()
				.setNsPrefixes(PrefixMapping.Extended)
				.setNsPrefixes(m)
				.setNsPrefix( "wbc", "http://environment.data.gov.uk/def/waterbody-classification/" )
				.setNsPrefix( "qb", "http://purl.org/linked-data/cube#" )
				.lock();
		
		Set<Property> predicates = m.listStatements().mapWith(Statement.Util.getPredicate).toSet();
		
		Aspects aspects = new Aspects();
		
		for (Property p: predicates) {
			Resource rangeType = findRangeType(m, p);
			String ID = p.getURI();
			String sn = pm.shortForm(ID);
			Aspect a = new Aspect(ID, new Shortname(pm, sn));
			if (rangeType != null) a.setRangeType(rangeType);
			aspects.include(a);
		}
		
		return new Example(pm, aspects, m);
	}
	
	static final Map1<RDFNode, String> getType = new Map1<RDFNode, String>() {

		@Override public String map1(RDFNode o) {
			if (o.isLiteral()) return o.asNode().getLiteralDatatypeURI();
			return null;
		}
		
	};
	
	private static Resource findRangeType(Model m, Property p) {
		Set<String> types = m.listStatements( null, p, (RDFNode) null ).mapWith(Statement.Util.getObject).mapWith(getType).toSet();
		return types.size() == 1 ? m.createResource( types.iterator().next() ) : null;
	}

	static Map<String, Example> examples = new HashMap<String, Example>();

	static void loadConfigs() {
		Map<String, Model> models = new HashMap<String, Model>();
		App a = AppConfig.getApp();
		for (String name: a.listComponentNames()) {
			if (name.startsWith("placeholder")) {
				Config c = (Config) a.getComponent(name);
				String filePath = AppConfig.theConfig.expandFileLocation(c.filePath);
				Model m = FileManager.get().loadModel(filePath);
				models.put(c.name, m);
			}
		}
	//
		examples.put("games", configureGames( models.get("games" ) ));
		examples.put("sprint2", configureICM( models.get("sprint2" ) ));
	}
	
	static { loadConfigs(); }
	
	@GET @Path("dataset") @Produces("application/json") public Response deliverDatasetNamesAsJSON() {
		JsonArray ja = new JsonArray();
		for (String name: examples.keySet()) ja.add(new JsonString(name) );
		return Response.ok(ja.toString(), "application/json").build();
	}
	
	@GET @Path("dataset.html") @Produces("text/html") public Response deliverDatasetNamesAsHTML() {
		StringBuilder links = new StringBuilder();
		for (String name: examples.keySet()) 
			links
				.append("<div>")
				.append( "<a href='dataset/" + name + "/submit" + "'>")
				.append(name)
				.append("</a>")
				.append("</div>")
				.append("\n");
		String entity = BunchLib.join
			( "<html>"
			, "<head>"
			, "<title>data API placeholder query submitter</title>"
			, "</head>"
			, "<body>"
			, "<h1>available examples</h1>"
			, links.toString()
			, "</body>"
			, "</html>"
			);
		return Response.ok(entity, "text/html").build();
	}
	
	@POST @Path("dataset/{name}/data") @Produces("text/plain") public Response placeholderPOST
		(@PathParam("name") String datasetName, @FormParam("json") String posted) {
		
		Example example = examples.get(datasetName);
		
		Problems p = new Problems();
		List<String> comments = new ArrayList<String>();
	
		comments.add( "datasetName: " + datasetName );
		comments.add( "posted JSON:\n" + posted + "\n");
		
		comments.add( "aspects:" );
		for (Aspect a: example.aspects.getAspects()) {
			Resource rt = a.getRangeType();
			comments.add( "  " + a + (rt == null ? "" : " [range: " + rt + "]" ) );
		}
		comments.add( "" );
				
		JsonObject jo = null;
		DataQuery q = null;
		String sq = null;
		Query qq = null;
		
		if (example == null) p.add("dataset '" + datasetName + "' not found." );
		
		try {
			jo = JSON.parse(posted);
			
			if (p.size() == 0) q = DataQueryParser.Do(p, example.pm, jo);
			
			if (p.size() == 0) sq = q.toSparql(p, example.aspects, example.pm);
			
			if (p.size() == 0) {
				try {
					qq = QueryFactory.create(sq);
					comments.add("Generated SPARQL:\n\n" + qq );
				} catch (Exception e) {
					p.add("Bad generated SPARQL:\n" + sq + "\n" + e.getMessage());
				}
			}
			
			List<String> vars = new ArrayList<String>();
			for (Aspect a: example.aspects.getAspects()) vars.add(a.asVar());
					
			if (p.size() == 0) {
				QueryExecution qe = QueryExecutionFactory.create( qq, example.model );
				ResultSet rs = qe.execSelect();
			
				JsonArray them = new JsonArray();
				while (rs.hasNext()) {
					// sb.append(rs.next()).append("\n");
					JsonObject row = Convert.toJson(vars, rs.next());
					them.add(row);
				}
				comments.add("resultset:\n\n" + them);
			}
			
		} catch (Exception e) {
			System.err.println("BROKEN: " + e);
			e.printStackTrace(System.err);
			p.add("BROKEN: " + e);
		}	
		
		if (p.size() == 0) {
				return Response.ok
					( "OK\n" 
					+ BunchLib.join(comments)		
					).build()
					;
			} else {
				return Response.ok
					( "FAILED:\n"
					+ BunchLib.join(comments)
					+ BunchLib.join(p.getProblemStrings())
					).build()
					;
			}	
	}
}
