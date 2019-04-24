${webResourceManager.requireResource("com.github.bayaro.prepare-versions:prepare-versions-resources")}

<div class="plugin-prepare-versions-container" data-provide="plugin-prepare-versions-page" data-page="form">

	<h1>Prepare Versions</h1>

	[#if environmentsList?has_content]

		<form class="aui">
			<fieldset>

				<h3>Choose environment to deploy it</h3>

				<div class="field-group plugin-prepare-versions-environments">
					<label>Environment name</label>
					<select class="select" name="dep2env" data-provide="environment-list">
						[#list environmentsList as env]
							<option value="${env}" [#if env == "xxx"] selected [/#if]>${env}</option>
						[/#list]
					</select>
                    <input class="aui-button" type="submit" value="Refresh"/>
				</div>

			</fieldset>
		</form>

		<table class="aui">
			<thead>
			<tr>
				<th>Aplication</th>
				<th>Builds</th>
			</tr>
			</thead>

			<tbody>
				[#list buildsList as buildProject]
				<tr>
					<td><a href="${baseUrl}/deploy/viewDeploymentProjectEnvironments.action?id=${buildProject.id}">${buildProject.name}</a></td>
					<td><input data-provide="select-project-checkbox" data-code="${buildProject.code}" type="checkbox"/></td>
				</tr>
				[/#list]
			</tbody>
		</table>

		<div class="plugin-prepare-versions-submit-container">
			<button class="aui-button aui-button-primary" data-provide="deploy-button" disabled>Prepare release & Deploy</button>
		</div>

	[#else]

		<p>It looks like you have no rights to do anything like Prepare Versions. Sorry</p>

	[/#if]

</div>
