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
    $("#query").click(function(){
      $.ajax({
        type: "POST",
        contentType: "application/json",
        data: $("#json").val(),
        dataType: "json",
        success: function(data){
            $("#results").html("<h2>Results</h2><pre>" + JSON.stringify(data, null, '    ') + "</pre>");
        },
        error: function(request, status, error) {
            $("#results").html("<h2>Failed</h2><p>" + error + "</p>");
        }
      });
    });
});
