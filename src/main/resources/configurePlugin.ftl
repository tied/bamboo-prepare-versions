[#-- @ftlvariable name="action" type="com.github.bayaro.ConfigurePluginAction" --]
[#-- @ftlvariable name="" type="com.github.bayaro.ConfigurePluginAction" --]

<html><head>
    <title>[@ww.text name='prepare-versions.plugin.name' /]</title>
    <meta name="decorator" content="adminpage">
    [#if success]
        <script type="application/javascript">
            require(['aui/flag'], function(Flag) {
                new Flag({ type: 'success', close: 'auto', body: '${action.getText("prepare-versions.config.storage.updated")}' });
            });
        </script>
	[#else]
        <script type="application/javascript">
            require(['aui/flag'], function(Flag) {
                new Flag({ type: 'error', body: '${errorMessage}' });
            });
        </script>
    [/#if]
</head><body>
[@ww.form action='savePluginConfiguration' namespace='/admin/plugins/prepare-versions'
    titleKey="prepare-versions.plugin.name" submitLabelKey='global.buttons.update']

    [@ui.bambooSection  titleKey='prepare-versions.config.storage-section' headerWeight='h3']
    	[@ww.textfield name='storageUrl' labelKey='prepare-versions.config.storage-url'
	    descriptionKey='prepare-versions.config.storage-url.description' /]
    [/@ui.bambooSection]
    [@ui.bambooSection  titleKey='prepare-versions.config.storage-auth' headerWeight='h3']
    	[@ww.textfield name='storageUsr' labelKey='prepare-versions.config.storage-usr'
	    descriptionKey='prepare-versions.config.storage-usr.description' /]
    	[@ww.textfield name='storagePwd' labelKey='prepare-versions.config.storage-pwd'
	    descriptionKey='prepare-versions.config.storage-pwd.description' /]
    [/@ui.bambooSection]
[/@ww.form]
</body></html>
