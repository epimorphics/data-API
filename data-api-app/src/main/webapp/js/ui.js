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

    // ------------
    // Edit support
    // ------------

    // Set up editable fields
    $.fn.editable.defaults.mode = 'inline';

    var editTarget = function(event) {
        return $(event.target).closest("button").attr("data-target");
    }

    var editRemoveAction = function(e){
        var rowid = editTarget(e);
        $(rowid).remove();
    };

    // Machinery to run edit-form interactions
    var makeEditCell = function(name, value) {
        return '<td><a href="#" class="ui-editable" data-type="text" data-inputclass="input-large" data-name="' + name + '">' + value + '</a></td>';
    }
    var makeEditRow = function(id, prop, value) {
        var row =
            '<tr id="$id">' + makeEditCell("prop", prop) + makeEditCell(prop, value)
            + '<td><button class="edit-remove-row  btn btn-sm" data-target="#$id"><span class="glyphicon glyphicon-minus-sign"></span></button>   \n'
            +     '<button class="edit-add-row btn btn-sm" data-target="#$id"><span class="glyphicon glyphicon-plus-sign"></span></button></td></tr>';
        row = row.replace(/\$id/g, id);
        return row;
    };

    var installEditRow = function(position, id, prop, value) {
        position.after( makeEditRow(id, prop, '') );
        $("#" + id).each(function(){
            $(this).find(".ui-editable").editable();
            $(this).find(".edit-remove-row").click( editRemoveAction );
            $(this).find(".edit-add-row").click( editAddAction );
            $(this).find('.ui-editable[data-name="prop"]').on('save', function(){
                var that = this;
                setTimeout(function() {
                    $(that).closest('td').next().find('.ui-editable').editable('show');
                }, 200);
            });
        });
    };

    var idcount = 1;

    var editAddAction = function(e){
        var row = $( editTarget(e) );
        var newid = row.attr("id") + idcount++;
        var prop = row.find("td:first").text();
        installEditRow(row, newid, prop, '""');
        return false;
    }

    var editAddNewAction = function(e) {
        var tableid = editTarget(e);
        var lastrow = $(tableid).find("tbody tr:last");
        var newid =  (tableid.replace(/^#/,'')) + "-newrow-"+ idcount++;
        installEditRow(lastrow, newid, '', '');
        return false;
    }

    $(".ui-editable").editable();
    $(".edit-remove-row").click( editRemoveAction );
    $(".edit-add-row").click( editAddAction );
    $(".edit-add-newrow").click( editAddNewAction );

    var emit = function(valin) {
        var val = $.trim(valin);
        var ch = val.charAt(0);
        if (ch === '[' || ch === '<' || ch === '"' || ch === "'") {
            return val;     // Already looks like Turtle
        }
        if (val.match(/^https?:/)) {
            return "<" + val + ">";
        } if (val.match(/^[+-]?[0-9]*(\.[0-9]+)?$/)) {
            return val;     // number
        } if (val.match(/^[a-zA-Z][\w\d\.]*:[\w\d\.:]*$/)) {
            return val;     // prefixed
        } else {
            return '"""' + val + '"""';
        }
    };

    // Implement edit save-changes functionality
    $(".edit-table-save").click( function(){
        var returnURL = $(this).attr("data-return");
        var isItem = $(this).attr("data-isitem");
        if (isItem) {
            isItem = isItem.toLowerCase() === "true";
        }
        var table = $("#edit-table");
        var data = $("#edit-prefixes").text();
        var url = table.attr("data-target");
        data = data + "\n<" + table.attr("data-root") + ">\n";
        table.find("tbody tr").each(function(){
            var row = $(this).find("td").toArray();
            var prop = emit($(row[0]).text());
            var value = emit( $(row[1]).text() );
            data = data + "    " + prop + " " + value + " ;\n";
        });
        $.ajax({
            type: (isItem ? "PATCH" : "PUT"),
            url: url,
            data: data,
            contentType: "text/turtle",
            success: function(){
                $("#msg").html("Submitted successfully");
                $('#msg-alert').removeClass('alert-warning').addClass('alert-success').show();
                window.location.href = returnURL;
            },
            error: function(xhr, status, error){
                $("#msg").html("Save failed: " + error + " - " + xhr.responseText);
                $('#msg-alert').removeClass('alert-success').addClass('alert-warning').show();
            }
          });
    });
});
