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

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bandana.BandanaManager;

public class PrepareVersionsForm extends BambooActionSupport {

    private final EnvironmentService environmentService;
    private final String baseUrl;

    private String dep2env;
    private Map<String, String> choosen = new HashMap<>();

    private List<String> environmentsList;
    private KnownEnvironmentBuilds buildsList;
    private Map<String, List<String>> deployedVersions;

    private Pattern patternRpm = Pattern.compile( "^<a href=\"([^\"]*)-([^\"-]*)-([^\"-]*)\\.x86_64\\.rpm\">.*" );
    private Pattern patternBranch = Pattern.compile( "^([^\\.]*)\\.([0-9a-f]*)$" );
    private Pattern patternInVersions = Pattern.compile( "^\\s*([^:]*):\\s*([^-]*)-([^\\s#$]*)[\\s#$].*" );

    private BandanaManager bandanaManager;

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
        final EnvironmentService environmentService,
        final BandanaManager bandanaManager
    ) {
        this.bandanaManager = bandanaManager;
        this.environmentService = environmentService;
        this.baseUrl = configurationAccessor.getAdministrationConfiguration().getBaseUrl();
    }

    @Override
    public String doDefault() throws Exception {

        environmentsList = getAllEnvironments();
        buildsList = getAllKnownBuilds();
        deployedVersions = getAllDeployedVersions( environmentsList );

        final HttpServletRequest request = ServletActionContext.getRequest();
        dep2env = request.getParameter( "dep2env" );
        for ( String project : buildsList.getProjects().keySet() ) {
            choosen.put( project, request.getParameter( project ) );
        }

        if ( dep2env == null ) {
            dep2env = ( environmentsList.size() > 0 ) ? environmentsList.get(0) : "";
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

    static public List<String> loadURL( String path, String storageUrl, String storageUsr, String storagePwd ) {
        final List<String> list = new ArrayList<>();
        BufferedReader reader = null;

        try {
            URL url = new URL( storageUrl + path );
            URLConnection uc = url.openConnection();
            if ( storageUsr != null && storageUsr != "" ) {
                String userpass = storageUsr;
                if ( storagePwd != null && storagePwd != "" ) {
                    userpass = userpass + ":" + storagePwd;
                }
                String basicAuth = "Basic " + new String( Base64.getEncoder().encode( userpass.getBytes() ));
                uc.setRequestProperty ( "Authorization", basicAuth );
            }
            reader = new BufferedReader( new InputStreamReader( uc.getInputStream() ) );
            String line;

            while (( line = reader.readLine() ) != null ) {
                list.add( line );
            }
        } catch ( FileNotFoundException e ) {
            list.add( "ERROR" );
            list.add( e.toString() );
        } catch ( IOException e ) {
            list.add( "ERROR" );
            list.add( e.toString() );
        } finally {
            try {
                if ( reader != null ) { reader.close(); }
            } catch ( IOException e ) {}
        }
        return list;
    }

    private List<String> loadURL( String path ) {
        return loadURL( path,
            String.valueOf( bandanaManager.getValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, ConfigurePluginAction.BANDANA_KEY_STORAGE_URL ) ),
            String.valueOf( bandanaManager.getValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, ConfigurePluginAction.BANDANA_KEY_STORAGE_USR ) ),
            String.valueOf( bandanaManager.getValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, ConfigurePluginAction.BANDANA_KEY_STORAGE_PWD ) )
        );
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

    @SuppressWarnings("unused")
    public Map<String, List<String>> getDeployedVersions() {
        return deployedVersions;
    }

    private void loadDeployedVersions( String envName, String uri, Map<String, List<String>> depVersions, String state ) {
        List<String> htmlBuilds = loadURL( uri );
        for ( String line : htmlBuilds ) {
             Matcher m = patternInVersions.matcher( line );
             if ( ! m.matches() ) continue;
             String version = m.group( 1 ) + "-" + m.group( 2 ) + "-" + m.group( 3 );
             List<String> envs = depVersions.get( version );
             if ( envs == null ) {
                 depVersions.put( version, new ArrayList<String>() );
                 envs = depVersions.get( version );
             }

             if ( state == "?" && envs.contains( envName + "+" ) ) {
                 envs.remove( envName + "+" );
                 envs.add( envName );
                 continue;
             }
             envs.add( envName + state );
        }
    }

    private Map<String, List<String>> getAllDeployedVersions( List<String> environments ) {
        final Map<String, List<String>> map = new TreeMap<>();

        //for (String envName : environments) {
        for (String envName : Arrays.asList( "dev1", "dev2", "dev3" , "stb", "prod" ) ) {
            loadDeployedVersions( envName, "/" + envName + "/versions.yaml.txt", map, "+" );
            loadDeployedVersions( envName, "/" + envName + "/versions.yaml.txt.started.txt", map, "?" );
        }
        return map;
    }
}
