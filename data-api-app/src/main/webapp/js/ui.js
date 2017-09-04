// Set of initialization actions run on page load to make UI features live

$(function() {

	// Auto-submit for select controls
	$("select.auto-submit").change(function() {
		$(this).closest("form").submit();
	});

	// Data table auto-config
	$(".data-table").dataTable();

	// Generic ajax actions
	$(".ajax-run").each(function() {
		var elt = $(this);
		var target = elt.attr('data-target');
		var action = elt.attr('data-action');
		elt.click(function() {
			$(target).load(action);
		});
	});

	// Query form - TODO move this to a separate file specific to the query
	// page?
	var formatTableEntry = function(value) {
		if ($.isArray(value)) {
			if (value.length === 0) {
				return "";
			}
			var acc = "[";
			$.each(value, function(index, value) {
				acc += (index > 0 ? ", " : "") + formatTableEntry(value)
			});
			acc += "]";
			return acc;
		} else {
			var v = value["@id"];
			if (v === undefined) {
				v = value["@value"];
			}
			if (v === undefined) {
				v = value;
			}
			return v;
		}
	};

	var formatTable = function(data, start) {
		var duration = now() - start;
		if (data.length > 0) {
			var html = "<p><em>Query time:</em> " + duration + " ms</p>";
			html += "<table class='table table-condensed table-striped table-bordered'>";
			html += "<thead><tr>";
			jQuery.each(data[0], function(key, value) {
				html += "<th>" + key + "</th>";
			});
			html += "</tr></thead><tbody>";
			for (var i = 0; i < data.length; i++) {
				html += "<tr>";
				jQuery.each(data[i], function(key, value) {
					html += "<td>" + formatTableEntry(value) + "</td>"
				});
				html += "</tr>";
			}
			html += "</tbody></table>";
			return html;
		} else {
			return "empty";
		}

	};

	var formatJSON = function(data, start) {
		var duration = now() - start;
		if (data.length > 0) {
			var html = "<p><em>Query time:</em> " + duration + " ms</p>";
			html += "<pre>" + JSON.stringify(data, null, '    ') + "</pre>";
			return html;
		} else {
			return "empty";
		}
	};

	var now = function() {
		return new Date().getTime();
	};

	var formatExplanation = function(data) {
		var theURL = data.grabURL;
		var html = data.status ? "<h3>Succeeded</h3>" : "<h3>Failed</h3>";
		if (!data.status) {
			html += "<pre>" + data.problems.replace(/</g, "&lt;") + "</pre>"
		}

		html += "<h3>Data set: " + data.datasetName + "</h3>";
		html += "<ul>";
		for (var i = 0; i < data.aspects.length; i++) {
			html += "<li>" + data.aspects[i] + "</li>";
		}
		html += "</ul>";
		html += "<h3>Request</h3><pre>" + data.request + "</pre>";
		html += "<h3>Query</h3>"
		html += "<em>target: " + data.sparqlQueryURL.replace(/</g, "&lt;") + "</em>";
		html += "<pre>" + data.sparql.replace(/</g, "&lt;") + "</pre>";
		// html += "<p>Processed in " + data.time + " ms</p>";
		return html;
	};

	var send = function(formatter, url) {
		return function() {
			var start = now();
			$.ajax({
				type : "POST",
				url : url,
				contentType : "application/json",
				data : $("#json").val(),
				dataType : "json",
				success : function(data) {
					data.grabURL = url;
					$("#results").html(
							"<h2>Results</h2>" + formatter(data, start));
				},
				error : function(xhr, status, error) {
					$("#results").html(
							"<h2>Failed</h2><p>" + error + ":"
									+ xhr.responseText + "</p>");
				}
			});
		};
	};

	$("#query-json").each(function() {
		var elt = $(this);
		var target = elt.attr('data-target');
		elt.click(send(formatJSON, target));
	});

	$("#query").each(function() {
		var elt = $(this);
		var target = elt.attr('data-target');
		elt.click(send(formatTable, target));
	});

	$("#explain").each(function() {
		var elt = $(this);
		var target = elt.attr('data-target');
		elt.click(send(formatExplanation, target));
	});

});
