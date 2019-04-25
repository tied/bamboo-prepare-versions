(function ($, window) {
    $(document).ready(function () {
        $("#versions select[name=dep2env]").change(function() {
            var curEnv = new RegExp( '[\\[ ]' + $(this).val() + '[,\\]]');
            var prodEnv = new RegExp( '[\\[ ]prod[,\\]]' );
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
    });
})(jQuery, window);
