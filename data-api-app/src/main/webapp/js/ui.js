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
        elt.click(function(){
            $(target).load(action);
        });
    });

    // Query form - TODO move this to a separate file specific to the query page?
    
    var formatTable = function(data) {
        var html = "<table class='table table-condensed table-striped table-bordered'><tbody>";
        for (var i = 0; i < data.length; i++) {
            html += "<tr>";
            jQuery.each(data[i], function(key, value){
                var v = value["@id"];
                if (v === undefined) {
                    v = value["@value"];
                }
                if (v === undefined) {
                    v = value;
                }
                html += "<td>" + v + "</td>"
            });
            html += "</tr>";
        }
        html +="</tbody></table>";
        return html;
    };
    
    var formatExplanation = function(data) {
        var html = "<h3>Data set: " + data.datasetName + "</h3>";
        html += "<ul>";
        for (var i = 0; i < data.aspects.length; i++) {
         html += "<li>" + data.aspects[i] + "</li>";
        }
        html += "</ul>";
        html += "<h3>Request</h3><pre>" + data.request + "</pre>";
        html += "<h3>Query</h3><pre>" + data.sparql.replace(/</g,"&lt;") + "</pre>";
        html +=  data.status ? "<h3>Succeeded</h3>" :"<h3>Failed</h3>";
        html += "<p>Processed in " + data.time + " ms</p>";
        return html;
    };

    var send = function(formatter, url) {
        return function() {  
            $.ajax({
                type: "POST",
                url: url,
                contentType: "application/json",
                data: $("#json").val(),
                dataType: "json",
                success: function(data) {
                    $("#results").html("<h2>Results</h2>" + formatter(data));
                },
                error: function(request, status, error) {
                    $("#results").html("<h2>Failed</h2><p>" + error + "</p>");
                }
            });
        };
    };
    
    $("#query-json").each(function() {
        var elt = $(this);
        var target = elt.attr('data-target');
        elt.click( send(function(data){return "<pre>" + JSON.stringify(data, null, '    ') + "</pre>"; }, target) );
    });
    
    $("#query").each(function() {
        var elt = $(this);
        var target = elt.attr('data-target');
        elt.click( send(formatTable, target) );
    });
    
    $("#explain").each(function() {
        var elt = $(this);
        var target = elt.attr('data-target');
        elt.click( send(formatExplanation, target) );
    });
    
});
