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
import com.atlassian.bamboo.user.BambooAuthenticationContext;
import com.atlassian.bamboo.plan.*;
import com.atlassian.user.User;

public class PrepareVersionsForm extends BambooActionSupport {

    private final EnvironmentService environmentService;
    private final String baseUrl;

    private String errorMessage = "";
    private String dep2proj = "FX";
    private String depByPlan = "deploy.versions";
    private String dep2env;
    private Map<String, String> choosen = new HashMap<>();

    private List<String> environmentsList;
    private KnownEnvironmentBuilds buildsList;
    private Map<String, List<String>> deployedVersions;
    private Set<String> branches = new TreeSet<>();

    private Pattern patternRpm = Pattern.compile( "^<a href=\"([^\"]*)-([^\"-]*)-([^\"-]*)\\.x86_64\\.rpm\">.*" );
    private Pattern patternBranch = Pattern.compile( "^([^\\.]*)\\.([0-9a-f]*)$" );
    private Pattern patternInVersions = Pattern.compile( "^\\s*([^:]*):\\s*([^-]*)-([^\\s#$]*)(\\s|#|$).*" );

    private BandanaManager bandanaManager;
    private BambooAuthenticationContext bambooAuthenticationContext;
    private PlanManager planManager;
    private PlanExecutionManager planExecutionManager;

    @SuppressWarnings("unused")
    public String getBaseUrl() { return baseUrl; }

    @SuppressWarnings("unused")
    public String getErrorMessage() { return errorMessage; }
    @SuppressWarnings("unused")
    public String getDep2proj() { return dep2proj; }
    @SuppressWarnings("unused")
    public String getDepByPlan() { return depByPlan; }
    @SuppressWarnings("unused")
    public String getDep2env() { return dep2env; }

    @SuppressWarnings("unused")
    public Map<String, String> getChoosen() { return choosen; }

    public PrepareVersionsForm(
        final AdministrationConfigurationAccessor configurationAccessor,
        final EnvironmentService environmentService,
        final BandanaManager bandanaManager,
        final BambooAuthenticationContext bambooAuthenticationContext,
        final PlanManager planManager,
        final PlanExecutionManager planExecutionManager
    ) {
        this.environmentService = environmentService;
        this.baseUrl = configurationAccessor.getAdministrationConfiguration().getBaseUrl();

        this.bandanaManager = bandanaManager;
        this.bambooAuthenticationContext = bambooAuthenticationContext;
        this.planManager = planManager;
        this.planExecutionManager = planExecutionManager;
    }

    private void logg( Object o ) {
        System.out.println( "++++++++++" + o );
    }

    private String executeBuild() {

        TopLevelPlan buildPlan = null;
        List<TopLevelPlan> plans = planManager.getAllPlans();

        for ( TopLevelPlan p : plans ) {
            if ( ! p.getProject().getName().equals( dep2proj ) ) continue;
            if ( ! p.getName().equals( dep2proj + " - " + depByPlan ) ) continue;
            buildPlan  = p;
            break;
        }

        if ( buildPlan == null ) {
            errorMessage = "Couldn't find the buildPlan '" + depByPlan + "' in the project '" + dep2proj + "'!";
            return Action.SUCCESS;
        }

        User user = bambooAuthenticationContext.getUser();

        Map<String,String> params = new HashMap<String, String>();
        Map<String,String> vars = new HashMap<String, String>();
        vars.put( "deploy_versions_2_environment", dep2env );
        for (Map.Entry<String, String> entry : choosen.entrySet()) {
            vars.put( "version_of_" + entry.getKey(), entry.getValue() );
        }

        planExecutionManager.startManualExecution( buildPlan, user, params, vars );
        return Action.SUCCESS;
    }

    @Override
    public String doDefault() throws Exception {
        errorMessage = "";

        environmentsList = getAllEnvironments();
        prepareAllKnownBuilds();
        deployedVersions = getAllDeployedVersions( environmentsList );

        final HttpServletRequest request = ServletActionContext.getRequest();
        dep2env = request.getParameter( "dep2env" );
        for ( String project : buildsList.getProjects().keySet() ) {
            choosen.put( project, request.getParameter( project ) );
        }

        if ( dep2env == null ) {
            dep2env = ( environmentsList.size() > 0 ) ? environmentsList.get(0) : "";
        }

        if ( "js".equals( request.getParameter( "atl_token_source" ) ) ) {
            return executeBuild();
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

    @SuppressWarnings("unused")
    public Set<String> getBranches() {
        return branches;
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

    private void prepareAllKnownBuilds() {
        KnownEnvironmentBuilds builds = new KnownEnvironmentBuilds( "builds" );
        Set<String> branches = new TreeSet<>();
        List<String> htmlBuilds = loadURL( "/builds" );
        Collections.sort( htmlBuilds, Collections.reverseOrder());

        for ( String line : htmlBuilds ) {
             Matcher m = patternRpm.matcher( line );
             if ( ! m.matches() ) continue;
             Matcher b = patternBranch.matcher( m.group( 3 ) );
             String branch = b.matches() ? b.group( 1 ) : "master";
             branches.add( branch.replace( "_", "-" ).replaceAll( "^(FX-[0-9]*)-", "$1/" ) );
             builds.addVersion( m.group( 1 ), branch, m.group( 2 ) + "-" + m.group( 3 ) );
        }
        this.branches = branches;
        this.buildsList = builds;
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
            String build = m.group( 1 ) + "-" + m.group( 2 ) + "-" + m.group( 3 );
            List<String> envs = depVersions.get( build );
            if ( envs == null ) {
                depVersions.put( build, new ArrayList<String>() );
                envs = depVersions.get( build );
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

        for (String envName : environments) {
            loadDeployedVersions( envName, "/" + envName + "/versions.yaml.txt", map, "+" );
            loadDeployedVersions( envName, "/" + envName + "/versions.yaml.txt.started.txt", map, "?" );
        }
        return map;
    }
}
