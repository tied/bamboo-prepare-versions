package com.github.bayaro;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.opensymphony.xwork2.Action;

import org.jetbrains.annotations.Nullable;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;

import java.util.List;
import com.atlassian.bamboo.deployments.environments.Environment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class PrepareVersionsForm extends BambooActionSupport {

    private final EnvironmentService environmentService;
    private final String baseUrl;

	private List<String> environmentsList = new ArrayList<>();
	private List<String> buildsList = new ArrayList<>();

    @Nullable
    @Override
    @SuppressWarnings("unused")
    public String getBaseUrl() {
        return baseUrl;
    }

    public PrepareVersionsForm(
        final AdministrationConfigurationAccessor configurationAccessor,
        final EnvironmentService environmentService
	) {

        this.environmentService = environmentService;

        environmentsList = getAllEnvironments();
        baseUrl = configurationAccessor.getAdministrationConfiguration().getBaseUrl();
    }

    @Override
    public String doDefault() throws Exception {
		return Action.SUCCESS;
	}

    @SuppressWarnings("unused")
    public List<String> getEnvironmentsList() {
        return environmentsList;
    }

    private List<String> getAllEnvironments() {
        final List<String> list = new ArrayList<>();

        try {
            Iterable<Environment> environments = environmentService.getAllEnvironments();
            for (Environment environment : environments) {
                list.add(environment.getName());
            }
        } catch (Exception ex) {
            // Not authorised
        }

        Collections.sort(list);

        return list.stream().distinct().collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public List<String> getBuildsList() {
        return buildsList;
    }

	private List<String> getAllBuilds() {
		final List<String> list = new ArrayList<>();
		return list;
	}

}
