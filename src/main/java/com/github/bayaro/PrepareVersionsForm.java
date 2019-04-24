package com.github.bayaro;

import com.atlassian.bamboo.ww2.BambooActionSupport;
import com.opensymphony.xwork2.Action;
import org.apache.struts2.ServletActionContext;
import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.Nullable;
import com.atlassian.bamboo.configuration.AdministrationConfigurationAccessor;
import com.atlassian.bamboo.deployments.environments.service.EnvironmentService;

import java.util.*;
import com.atlassian.bamboo.deployments.environments.Environment;
import java.util.stream.Collectors;

import java.io.*;
import java.net.*;
import java.util.regex.*;

public class PrepareVersionsForm extends BambooActionSupport {

    private final EnvironmentService environmentService;
    private final String baseUrl;

    private String dep2env;
    private Map<String, String> choosen = new HashMap<>();

    private List<String> environmentsList;
    private KnownEnvironmentBuilds buildsList;

    private Pattern patternRpm = Pattern.compile( "^<a href=\"([^\"]*)-([^\"-]*)-([^\"-]*)\\.x86_64\\.rpm\">.*" );
    private Pattern patternBranch = Pattern.compile( "^([^\\.]*)\\.([0-9a-f]*)$" );

    @Nullable
    @Override
    @SuppressWarnings("unused")
    public String getBaseUrl() {
        return baseUrl;
    }

    @SuppressWarnings("unused")
    public String getDep2env() {
        return dep2env;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getChoosen() {
        return choosen;
    }

    public PrepareVersionsForm(
        final AdministrationConfigurationAccessor configurationAccessor,
        final EnvironmentService environmentService
    ) {
        this.environmentService = environmentService;
        this.baseUrl = configurationAccessor.getAdministrationConfiguration().getBaseUrl();
    }

    @Override
    public String doDefault() throws Exception {

        environmentsList = getAllEnvironments();
        buildsList = getAllKnownBuilds();

        final HttpServletRequest request = ServletActionContext.getRequest();
        dep2env = request.getParameter( "dep2env" );
        if ( dep2env == null ) dep2env = "";
        for ( String project : buildsList.getProjects().keySet() ) {
            System.out.println( "****** " + project + " == " + request.getParameter( project ) );
            choosen.put( project, request.getParameter( project ) );
        }

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
                list.add( environment.getName() );
            }
        } catch (Exception ex) {
            // Not authorised
        }

        Collections.sort(list);

        return list.stream().distinct().collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public KnownEnvironmentBuilds getBuildsList() {
        return buildsList;
    }

    private List<String> loadURL( String uri ) {
        final List<String> list = new ArrayList<>();
        BufferedReader reader = null;

        try {
            // TODO
            String basicAuth = "Basic " + new String( Base64.getEncoder().encode( userpass.getBytes() ));
            uc.setRequestProperty ( "Authorization", basicAuth );
            reader = new BufferedReader( new InputStreamReader( uc.getInputStream() ) );
            String line;

            while (( line = reader.readLine() ) != null ) {
                list.add( line );
            }
        } catch ( FileNotFoundException e ) {
                e.printStackTrace();
        } catch ( IOException e ) {
                e.printStackTrace();
        } finally {
            try {
                if ( reader != null ) {
                    reader.close();
                }
            } catch ( IOException e ) {}
        }

        return list;
    }

    private KnownEnvironmentBuilds getAllKnownBuilds() {
        KnownEnvironmentBuilds builds = new KnownEnvironmentBuilds( "builds" );
        List<String> htmlBuilds = loadURL( "/builds" );
        Collections.sort( htmlBuilds, Collections.reverseOrder());

        for ( String line : htmlBuilds ) {
             Matcher m = patternRpm.matcher( line );
             if ( ! m.matches() ) continue;
             Matcher b = patternBranch.matcher( m.group( 3 ) );
             String branch = b.matches() ? b.group( 1 ) : "master";
             builds.addVersion( m.group( 1 ), branch, m.group( 2 ) + "-" + m.group( 3 ) );
        }
        return builds;
    }
}
