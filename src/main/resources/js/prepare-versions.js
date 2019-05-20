(function ($, window) {
    $(document).ready(function () {
        $("#versions input[type=submit][value=Refresh]").click( function() {
            $("<form action='" + document.location.pathname + "'>"
                + "<input type=hidden name=dep2env value='" + $("#versions select[name=dep2env]").val() + "'></form>").appendTo('body').submit();
            return false;
        });
        $("#versions select[name=dep2env]").change(function() {
            var curEnv = new RegExp( '[\\[ ]' + $(this).val() + '[\?,\\]]');
            var prodEnv = new RegExp( '[\\[ ]prod[\?,\\]]' );
            $("#versions select.project").each( function() {
                var options = $(this).children("option").filter( function() {
                    return curEnv.test( $(this).text() );
                });
                if ( options.length == 0 ) {
                    options = $(this).children("option").filter( function() {
                        return prodEnv.test( $(this).text() );
                    });
                }
                if ( options.length == 0 ) {
                    options = $(this).children("option[value=branch-master]").next();
                }
                if ( options.length == 0 ) {
                    options = $(this).children("option:first").next();
                }
                options.prop('selected', 'selected');
            });
        });
        $("#versions a.b").click(function() {
            $("#versions select.project option[value=branch-" + $(this).text().replace( /[^a-zA-Z0-9]/g, '_' ) + "]").next().prop('selected', 'selected');
        });
        $("#versions a.e").click(function() {
            var curEnv = new RegExp( '[\\[ ]' + $(this).text() + '[\?,\\]]');
            $("#versions select.project option").filter( function() {
                return curEnv.test( $(this).text() );
            }).prop('selected', 'selected');
        });
    });
})(jQuery, window);
