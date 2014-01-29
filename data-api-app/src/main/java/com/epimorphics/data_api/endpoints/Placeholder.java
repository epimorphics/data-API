/*                                                                                                                            
    LICENCE summary to go here.                                                                                        
    
    (c) Copyright 2014 Epimorphics Limited
 */

package com.epimorphics.data_api.endpoints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.epimorphics.data_api.conversions.ResultsToJson;
import com.epimorphics.data_api.conversions.ResultsToJson.JSONConsumer;
import com.epimorphics.data_api.data_queries.DataQuery;
import com.epimorphics.data_api.data_queries.DataQueryParser;
import com.epimorphics.data_api.libs.BunchLib;
import com.epimorphics.data_api.libs.JSONLib;
import com.epimorphics.data_api.reporting.Problems;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
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

		Example example = examples.get(datasetName);

		Problems p = new Problems();
		List<String> comments = new ArrayList<String>();

		comments.add("datasetName: " + datasetName);
		comments.add("posted JSON:\n" + posted + "\n");

		comments.add("aspects:");
		for (Aspect a : example.aspects.getAspects()) {
			Resource rt = a.getRangeType();
			boolean optional = a.getIsOptional(), multiple = a
					.getIsMultiValued();
			comments.add("  " + a + (rt == null ? "" : " [range: " + rt + "]")
					+ (optional ? ", optional" : "")
					+ (multiple ? ", multivalued" : ""));
		}
		comments.add("");

		if (example == null)
			p.add("dataset '" + datasetName + "' not found.");

		try {
			List<Aspect> aspects = new ArrayList<Aspect>();
			for (Aspect a : example.aspects.getAspects())
				aspects.add(a);

			JsonObject jo = JSON.parse(posted);

			DataQuery q = null;
			String sq = null;

			if (p.isOK())
				q = DataQueryParser.Do(p, example.pm, jo);

			if (p.isOK())
				sq = q.toSparql(p, example.aspects, example.pm);

			checkLegalSPARQL(p, sq);
			
			if (p.isOK()) comments.add("generated SPARQL:\n\n" + QueryFactory.create(sq));

			if (p.isOK()) {
				ResultSet rs = example.source.select(sq);
				final JsonArray result = new JsonArray();
				JSONConsumer toResult = JSONLib.consumeToArray(result);
				ResultsToJson.convert(aspects, toResult, rs);
				comments.add("resultset:\n\n" + result);
			}

		} catch (Exception e) {
			System.err.println("BROKEN: " + e);
			e.printStackTrace(System.err);
			p.add("BROKEN: " + e);
		}

		if (p.isOK()) {
			return Response.ok("OK\n" + BunchLib.join(comments)).build();
		} else {
			return Response.ok(
					"FAILED:\n" + BunchLib.join(comments)
							+ BunchLib.join(p.getProblemStrings())).build();
		}
	}

	@POST @Path("dataset/{name}/data") @Produces("application/json") public Response placeholder_query_POST(
			@PathParam("name") String datasetName,
			@FormParam("json") String posted) {

		Example example = examples.get(datasetName);

		Problems p = new Problems();
		final JsonArray result = new JsonArray();

		if (example == null)
			p.add("dataset '" + datasetName + "' not found.");

		try {
			List<Aspect> aspects = new ArrayList<Aspect>();
			for (Aspect a : example.aspects.getAspects())
				aspects.add(a);

			JsonObject jo = JSON.parse(posted);

			DataQuery q = null;
			String sq = null;

			if (p.isOK())
				q = DataQueryParser.Do(p, example.pm, jo);

			if (p.isOK())
				sq = q.toSparql(p, example.aspects, example.pm);

			checkLegalSPARQL(p, sq);

			if (p.isOK()) {
				ResultSet rs = example.source.select(sq);
				JSONConsumer toResult = JSONLib.consumeToArray(result);
				ResultsToJson.convert(aspects, toResult, rs);
			}

		} catch (Exception e) {
			System.err.println("BROKEN: " + e);
			e.printStackTrace(System.err);
			p.add("BROKEN: " + e);
		}

		if (p.isOK()) {
			return Response.ok(result.toString()).build();

		} else {
			return Response.ok("FAILED:\n" + BunchLib.join(p.getProblemStrings())).build();
		}
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
