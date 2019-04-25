${webResourceManager.requireResource("com.github.bayaro.prepare-versions:prepare-versions-resources")}

<div class="plugin-prepare-versions-container" data-provide="plugin-prepare-versions-page" data-page="form">

  <h1>Prepare Versions</h1>

  [#if environmentsList?has_content]

    <form id="versions">
      <fieldset>

        <h3>Choose environment to deploy it</h3>

        <div class="field-group plugin-prepare-versions-environments">
          <label>Environment name</label>
          <select class="select" name="dep2env" data-provide="environment-list">
            [#list environmentsList as env]
              <option value="${env}" [#if env == dep2env] selected [/#if]>${env}</option>
            [/#list]
          </select>
          <button class="aui-button aui-button-primary" data-provide="deploy-button">Prepare versions & Deploy</button>
          <input class="aui-button" type="submit" value="Refresh"/>
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
                  <option value="${v}" [#if (choosen[k]?? && v == choosen[k]) || (deployedVersions[k+"-"+v]?? && deployedVersions[k+"-"+v].contains(dep2env))] selected [/#if]>[#if deployedVersions[k+"-"+v]??]${deployedVersions[k+"-"+v]} [/#if]${v}</option>
                [/#list]
              [/#list]
            </select></td>
        </tr>
        [/#list]
      </tbody>
    </table>

    <div class="plugin-prepare-versions-submit-container">
            <br>
    </div>

    </form>

  [#else]

    <p>It looks like you have no rights to do anything like Prepare Versions. Sorry</p>

  [/#if]

</div>
