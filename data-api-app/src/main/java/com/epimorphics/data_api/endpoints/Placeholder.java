/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
 */

package com.epimorphics.data_api.endpoints;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.jena.atlas.json.*;

import com.epimorphics.appbase.core.App;
import com.epimorphics.appbase.core.AppConfig;

import com.epimorphics.data_api.aspects.Aspect;
import com.epimorphics.data_api.aspects.Aspects;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.reporting.Problems;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

// placeholder endpoint, has a fake setup rather than a proper
// configuration.
@Path("placeholder") public class Placeholder {

	static Map<String, Example> examples = new HashMap<String, Example>();

	static void loadConfigs() {
		Map<String, Model> models = new HashMap<String, Model>();
		App a = AppConfig.getApp();
		for (String name : a.listComponentNames()) {
			if (name.startsWith("placeholder")) {
				Config c = (Config) a.getComponent(name);
				String filePath = AppConfig.theConfig
						.expandFileLocation(c.filePath);
				Model m = FileManager.get().loadModel(filePath);
				models.put(c.name, m);
			}
		}
		//
		examples.put("games", Example_Games.configureGames(models.get("games")));
		examples.put("sprint2",	Example_Legacy.configureLegacy(models.get("sprint2")));
	}

	static { loadConfigs(); }

	@GET @Path("dataset") @Produces("application/json")	public Response deliverDatasetNamesAsJSON() {
		JsonArray ja = new JsonArray();
		for (String name : examples.keySet())
			ja.add(new JsonString(name));
		return Response.ok(ja.toString(), "application/json").build();
	}

	@GET @Path("dataset.html") @Produces("text/html") public Response deliverDatasetNamesAsHTML() {
		StringBuilder links = new StringBuilder();
		for (String name : examples.keySet())
			links.append("<div>")
					.append("<a href='/data-api/offer-query-submission?name="
							+ name + "'>").append(name).append("</a>")
					.append("</div>").append("\n");
		//
		String entity = BunchLib.join("<html>", "<head>",
				"<title>data API placeholder query submitter</title>",
				"</head>", "<body>", "<h1>available examples</h1>",
				links.toString(), "</body>", "</html>");
		return Response.ok(entity, "text/html").build();
	}

	@POST @Path("dataset/{name}/explain-query") @Produces("text/plain")	public Response placeholder_explain_POST(
			@PathParam("name") String datasetName,
			@FormParam("json") String posted) {
		
		return Response.serverError().build();

//		Example example = examples.get(datasetName);
//
//		Problems p = new Problems();
//		List<String> comments = new ArrayList<String>();
//
//		comments.add("datasetName: " + datasetName);
//		comments.add("posted JSON:\n" + posted + "\n");
//
//		comments.add("aspects:");
//		for (Aspect a : example.aspects.getAspects()) {
//			Resource rt = a.getRangeType();
//			boolean optional = a.getIsOptional(), multiple = a
//					.getIsMultiValued();
//			comments.add("  " + a + (rt == null ? "" : " [range: " + rt + "]")
//					+ (optional ? ", optional" : "")
//					+ (multiple ? ", multivalued" : ""));
//		}
//		comments.add("");
//
//		if (example == null)
//			p.add("dataset '" + datasetName + "' not found.");
//
//		try {
//			List<Aspect> aspects = new ArrayList<Aspect>();
//			for (Aspect a : example.aspects.getAspects())
//				aspects.add(a);
//
//			JsonObject jo = JSON.parse(posted);
//
//			DataQuery q = null;
//			String sq = null;
//
//			if (p.isOK())
//				q = DataQueryParser.Do(p, example.pm, jo);
//
//			if (p.isOK())
//				sq = q.toSparql(p, example.aspects, null, example.pm);
//
//			checkLegalSPARQL(p, sq);
//			
//			if (p.isOK()) comments.add("generated SPARQL:\n\n" + QueryFactory.create(sq));
//
//			if (p.isOK()) {
//				ResultSet rs = example.source.select(sq);
//				final JsonArray result = new JsonArray();
//				JSONConsumer toResult = JSONLib.consumeToArray(result);
//				ResultsToJson.convert(aspects, toResult, rs);
//				comments.add("resultset:\n\n" + result);
//			}
//
//		} catch (Exception e) {
//			System.err.println("BROKEN: " + e);
//			e.printStackTrace(System.err);
//			p.add("BROKEN: " + e);
//		}
//
//		if (p.isOK()) {
//			return Response.ok("OK\n" + BunchLib.join(comments)).build();
//		} else {
//			return Response.ok
//				( "FAILED:\n" + BunchLib.join(comments)	+ BunchLib.join(p.getProblemStrings())).build();
//		}
	}
	
	static final Map<String, Lookback> lookbacks = new HashMap<String, Lookback>();
	
	@GET @Path("lookback/{token}") @Produces("text/plain") public Response lookback_at_query
		( @Context HttpServletRequest req
		, @PathParam("token") String token
		) {
		
		return Response.serverError().build();
		
//		Lookback l = lookbacks.get(token);
//		
////		String skel = BunchLib.join
////			( "<html>"
////			, "<head>"
////			, "</head>"
////			, "</body>"
////			, "<h1>lookback:</h1>"
////			, l.toText()
////			, "</body>"
////			, "</html>"
////			);
//			
//		return Response.ok(l.toText()).build();
//	}
//
//	@POST @Path("dataset/{name}/data") @Produces("application/json") public Response placeholder_query_POST(
//			@PathParam("name") String datasetName
//			, @FormParam("json") String posted
//			, @Context HttpServletRequest req
//			, @QueryParam("token") String token
//			) throws UnsupportedEncodingException {
//	//
//		Lookback l = new Lookback();
//		if (token == null) token = UUID.randomUUID().toString();		
//		lookbacks.put(token, l);
//		l.addComment( "Dataset name", datasetName );
//		l.addComment( "JSON-coded query", posted );
//	//
//		Example example = examples.get(datasetName);
//		List<Restriction> restrictions = example.restrictions();
//		Problems p = new Problems();
//		final JsonArray result = new JsonArray();
//	//
//		if (example == null)
//			p.add("dataset '" + datasetName + "' not found.");
//
//		try {
//			List<Aspect> aspects = new ArrayList<Aspect>();
//			for (Aspect a : example.aspects.getAspects())
//				aspects.add(a);
//
//			addAspectComments(l, example.aspects );
//			
//			JsonObject jo = JSON.parse(posted);
//
//			DataQuery q = null;
//			String sq = null;
//
//			if (p.isOK())
//				q = DataQueryParser.Do(p, example.pm, jo);
//
//			if (p.isOK()) {
//				sq = q.toSparql(p, example.aspects, null, example.pm);
//			}
//			
//			l.addComment("Generated SPARQL", sq);
//
//			checkLegalSPARQL(p, sq);
//
//			if (p.isOK()) {
//				ResultSet rs = example.source.select(sq);
//				JSONConsumer toResult = JSONLib.consumeToArray(result);
//				ResultsToJson.convert(aspects, toResult, rs);
//			}
//
//		} catch (Exception e) {
//			System.err.println("BROKEN: " + e);
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			PrintStream ps = new PrintStream(os);
//			e.printStackTrace(ps);
//			ps.flush();
//			p.add("BROKEN: " + e + "\n" + os.toString("UTF-8"));
//		}
//				
//		if (p.isOK()) {
//			return Response.ok(result.toString()).header("x-epi-token", token).build();
//
//		} else {
//			String problemStrings = p.getProblemStrings();
//			l.addComment("Problems detected", problemStrings );
//			return Response.serverError().header("x-epi-token", token).entity("FAILED:\n" + BunchLib.join(problemStrings)).build();
//		}
	}

	private void addAspectComments(Lookback l, Aspects aspects) {		
		StringBuilder comment = new StringBuilder();
		
		for (Aspect a : aspects.getAspects()) {
			Resource rt = a.getRangeType();
			boolean optional = a.getIsOptional(), multiple = a.getIsMultiValued();
		//
			comment.append( "  " ).append( a ).append( (rt == null ? "" : " [range: " + rt + "]") );
			comment.append( (optional ? ", optional" : "") );
			comment.append( (multiple ? ", multivalued" : "") );
			comment.append( "\n" );
		}
		l.addComment("aspects", comment.toString());
	}

	private void checkLegalSPARQL(Problems p, String sq) {
		if (p.isOK()) {
			try {
				QueryFactory.create(sq);
			} catch (Exception e) {
				p.add("Bad generated SPARQL:\n" + sq + "\n" + e.getMessage());
			}
		}
	}
}
