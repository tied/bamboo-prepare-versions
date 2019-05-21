${webResourceManager.requireResource("com.github.bayaro.prepare-versions:prepare-versions-resources")}

    [#if errorMessage != ""]
        <script type="application/javascript">
            require(['aui/flag'], function(Flag) {
                new Flag({ type: 'error', body: '${errorMessage}' });
            });
        </script>
    [/#if]


<div class="plugin-prepare-versions-container" data-provide="plugin-prepare-versions-page" data-page="form">

  <h1>${i18n.getText('prepare-versions.plugin.name')}</h1>

  [#if errorMessage != ""]
    <label class='error'>ERROR: ${errorMessage}</label>
  [/#if]

  [#if environmentsList?has_content]

    <form id="versions">
      <input type="hidden" name="planKey" value="[#if buildPlan??]${buildPlan.getPlanKey()}[/#if]">
      <fieldset>

        <div class="field-group plugin-prepare-versions-environments">
          <label>In project:</label> <b>${dep2proj}</b>
          <label>with plan:</label> <b>${depByPlan}</b>
          <label>for environment:</label>
          <select class="select" name="dep2env" data-provide="environment-list">
            [#list environmentsList as env][#if env != 'prod']
              <option value="${env}" [#if env == dep2env] selected [/#if]>${env}</option>
            [/#if][/#list]
          </select>
          <button id="prepare" class="aui-button aui-button-primary" data-provide="deploy-button">Prepare versions & Deploy</button>
          <input class="aui-button" type="submit" value="Refresh"/>
        </div>

        <hr>
        <div class="field-group plugin-prepare-versions-environments">
          <label>Deploy branch deployed to</label> &middot;[#list environmentsList as env]&middot; <a class="e">${env}</a> &middot;[/#list]
        </div>
        <div class="field-group plugin-prepare-versions-environments">
          <label>Deploy branch</label> &middot; <a class="b">master</a> &middot;[#list branches as b][#if b != 'master']&middot; <a class="b">${b}</a> &middot;[/#if][/#list]
        </div>

      </fieldset>

    <table class="aui">
      <thead>
      <tr>
        <th>Aplication</th>
        <th>Builds</th>
      </tr>
      </thead>

      <tbody>
        [#list buildsList.projects?keys as k]
        <tr>
          <td><a _target="blank" href="${baseUrl}/bamboo/browse/${buildsList.projects[k].name}">${k}</a></td>
            <td><select class="select project" name="${k}">
              [#list buildsList.projects[k].branches?keys as b]
                <option value="branch-${b}" disabled>${b}</option>
                [#list buildsList.projects[k].branches[b] as v]
                  <option value="${v}" [#if (choosen[k]?? && v == choosen[k]) || (deployedVersions[k+"-"+v]?? && (deployedVersions[k+"-"+v].contains(dep2env + "?") || deployedVersions[k+"-"+v].contains(dep2env)))] selected [/#if]>[#if deployedVersions[k+"-"+v]??]${deployedVersions[k+"-"+v]} [/#if]${v}</option>
                [/#list]
              [/#list]
            </select></td>
        </tr>
        [/#list]
      </tbody>
    </table>

    </form>

  [#else]

    <p>It looks like you have no rights to do anything like Prepare Versions. Sorry</p>

  [/#if]

</div>
