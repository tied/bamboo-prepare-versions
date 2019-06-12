(function ($, window) {
    $(document).ready(function () {
        $("#versions #prepare").click(function() {
            try {
				var form = '<input type="hidden" name="planKey" value="' + $("#versions input[name=planKey]").val() + '">'
                    + '<input type="hidden" name="key_release" value="release">'
                    + '<input type="hidden" name="variable_release" value="' + $("#versions input[name=releaseName]").val() + '">'
                    + '<input type="hidden" name="key_dep2env" value="deploy_2">'
                    + '<input type="hidden" name="variable_dep2env" value="' + $("#versions select[name=dep2env]").val() + '">';
                var ki = 1;
                $("#versions select.project").each(function() {
                    var idx = String("0" + ki).slice(-2);
					form += '<input type="hidden" name="key_' + idx + '" value="' + idx + '">'
					 + '<input type="hidden" name="variable_' + idx + '" value="' + $(this).attr("name") + ": " + $(this).val() + '">';
                    ++ki;
                });
                $('<form action="ajax/runParametrisedManualBuild.action" method="POST">' + form + '</form>').appendTo($(document.body)).submit();
            } catch(err) {
                console.log(err);
            }
            return false;
        });
        $("#versions input[type=submit][value=Refresh]").click(function() {
            $("<form action='" + document.location.pathname + "'>"
                + "<input type=hidden name=dep2env value='" + $("#versions select[name=dep2env]").val() + "'></form>").appendTo('body').submit();
            return false;
        });
        $("#versions select[name=dep2env]").change(function() {
            var curEnv = new RegExp('[\\[ ]' + $(this).val() + '[\?,\\]]');
            var prodEnv = new RegExp('[\\[ ]prod[\?,\\]]');
            $("#versions select.project").each(function() {
                var options = $(this).children("option").filter(function() {
                    return curEnv.test($(this).text());
                });
                if (options.length == 0) {
                    options = $(this).children("option").filter(function() {
                        return prodEnv.test($(this).text());
                    });
                }
                if (options.length == 0) {
                    options = $(this).children("option[value=branch-master]").next();
                }
                if (options.length == 0) {
                    options = $(this).children("option:first").next();
                }
                options.prop('selected', 'selected').parent().change();
            });
        });
        $("#versions a.b").click(function() {
            var bn = $(this).parent().attr("data");
            if (bn != "") bn += "_";
            bn += $(this).text();
            $("#versions select.project option[value=branch-" + bn.replace(/[^a-zA-Z0-9]/g, '_') + "]").next()
                .prop('selected', 'selected').parent().change();
            $("#versions input[name=releaseName]").val($(this).text());
        });
        $("#versions a.e").click(function() {
            var curEnv = new RegExp('[\\[ ]' + $(this).text() + '[\?,\\]]');
            $("#versions select.project option").filter(function() {
                return curEnv.test($(this).text());
            }).prop('selected', 'selected').parent().change();
            var comm = ($("#versions select[name=dep2env]").val() == $(this).text()) ? "Redeploy ???"
                : "Coping the " + $(this).text() + " environment";
            $("#versions input[name=releaseName]").val(comm);
        });
        $("#versions input[name=releaseName]").click(function() {
            $(this).select();
        });
        $("#versions select.project").change(function() {
            var curEnv = new RegExp('[\\[ ]' + $("#versions select[name=dep2env]").val() + '[\?,\\]]');
            $(this).closest('tr').find('div.l').html('');
            var a = $(this).closest('tr').find('a.sc');
            a.css('display', curEnv.test($(this).children('option:selected').text()) ? 'none' : 'inline').text(a.attr('show'));
        });
        $("#versions iframe").load(function() {
            $(this).closest('tr').find('div.l').html($(this).contents().find('section#content div div').html());
            var a = $(this).closest('tr').find('a.sc');
            a.text(a.attr('hide'));
        });
        $("#versions a.sc").click(function() {
            var d = $(this).closest('tr').find('div.l');
            if (d.children().length > 0) {
                d.html('');
                $(this).text($(this).attr('show'));
                return;
            }
            var curEnv = new RegExp('[\\[ ]' + $("#versions select[name=dep2env]").val() + '[\?,\\]]');
            var sel = $(this).closest('tr').find('select.project');
            var deployed = sel.find('option').filter(function() {
                return curEnv.test($(this).text());
            });
            deployed = ( deployed.length > 0 ) ? $(deployed.get(0)).attr('value') : '';
            $(this).closest('tr').find('iframe').attr('src','getCommitsList.action?'
                + 'p=' + sel.attr('name') + '&wanted=' + sel.val() + '&deployed=' + deployed
            );
        });
        $("#versions select.project").change();
    });
})(jQuery, window);
