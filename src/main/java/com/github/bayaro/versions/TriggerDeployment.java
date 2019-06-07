package com.github.bayaro.versions;

// based on https://github.com/vicsz/Bamboo-Deployment-Trigger-Task-Plugin
import java.util.*;
import org.jetbrains.annotations.NotNull;
import com.atlassian.bamboo.task.*;
import com.atlassian.bamboo.user.*;
import com.atlassian.bamboo.build.logger.*;
import com.atlassian.bamboo.deployments.environments.*;
import com.atlassian.bamboo.deployments.execution.*;
import com.atlassian.bamboo.deployments.execution.service.*;
import com.atlassian.bamboo.deployments.execution.triggering.*;
import com.atlassian.bamboo.deployments.projects.*;
import com.atlassian.bamboo.deployments.projects.service.*;
import com.atlassian.bamboo.deployments.versions.*;
import com.atlassian.bamboo.deployments.versions.service.*;
import com.atlassian.bamboo.deployments.results.*;
import com.atlassian.bamboo.deployments.results.service.*;
import com.atlassian.bamboo.plan.*;
import com.atlassian.bamboo.variable.*;
import com.atlassian.bamboo.v2.build.*;
import com.atlassian.bamboo.v2.build.trigger.*;
import com.atlassian.user.User;

import com.atlassian.bamboo.chains.*;
import com.atlassian.bamboo.chains.plugins.*;
import com.atlassian.bamboo.plan.cache.*;

public class TriggerDeployment implements PostStageAction {

    private DeploymentExecutionService deploymentExecutionService;
    private DeploymentVersionService deploymentVersionService;
    private DeploymentProjectService deploymentProjectService;
    private DeploymentResultService deploymentResultService;
    private EnvironmentTriggeringActionFactory triggeringActionFactory;
    private BambooUserManager bambooUserManager;
    private BuildLogger buildLogger = null;

    private void logg( Object o ) {
        System.out.println( "++++++++++ " + o );
        buildLogger.addErrorLogEntry( o.toString() );
    }

    public TriggerDeployment(
            DeploymentVersionService deploymentVersionService,
            DeploymentExecutionService deploymentExecutionService,
            final DeploymentResultService deploymentResultService,
            final EnvironmentTriggeringActionFactory triggeringActionFactory,
            final DeploymentProjectService deploymentProjectService,
            final BambooUserManager bambooUserManager
    ) {
        this.deploymentProjectService = deploymentProjectService;
        this.deploymentResultService = deploymentResultService;
        this.triggeringActionFactory = triggeringActionFactory;
        this.deploymentVersionService = deploymentVersionService;
        this.bambooUserManager = bambooUserManager;
        this.deploymentExecutionService = deploymentExecutionService;
    }

    public void execute(ChainResultsSummary chainResultsSummary, ChainStageResult chainStageResult, StageExecution stageExecution) {
        ImmutablePlan p = chainResultsSummary.getImmutablePlan();
        if ( ! p.getProject().getName().equals( PrepareVersionsForm.dep2proj ) ) return;
        if ( ! p.getName().equals( PrepareVersionsForm.dep2proj + " - " + PrepareVersionsForm.depByPlan ) ) return;

        buildLogger = p.getBuildLogger();

        if ( ! stageExecution.isSuccessful() ) {
            logg( "NOTICE: Build faild. Deployment is skipped" );
            return;
        }

		List<BuildExecution> buildsList = stageExecution.getBuilds();
		BuildContext topBuildContext = buildsList.get( buildsList.size() - 1 ).getBuildContext();
		while ( topBuildContext.getParentBuildContext() != null ) {
			topBuildContext = topBuildContext.getParentBuildContext();
		}

        logg( "-------------------------------------" );
        Map<String, String> params = new TreeMap();
        for (Map.Entry<String, VariableDefinitionContext> entry : topBuildContext.getVariableContext().getOriginalVariables().entrySet() ) {
            params.put( entry.getKey(), entry.getValue().getValue() );
        }

        for (Map.Entry<String, String> entry : params.entrySet() ) {
            logg( entry.getKey() + ": " + entry.getValue() );
        }
        logg( "=====================================" );

        String dep2env = params.get( "dep2env" );
        String releaseName = params.get( "release" );

		PlanKey buildPlanKey = topBuildContext.getTypedPlanKey();

        Environment targetEnvironment = null;
        DeploymentProject deploymentProject = null;
        for( DeploymentProject dp : deploymentProjectService.getDeploymentProjectsRelatedToPlan( buildPlanKey ) ) {
            for( Environment e : dp.getEnvironments() ) {
                if ( e.getName().equals( dep2env ) ) {
                    deploymentProject = dp;
                    targetEnvironment = e;
                    break;
                }
            }
        }
        if ( targetEnvironment == null ) {
            logg( "Couldn't find environment '" + dep2env + "' ..." );
            return;
        }

        waitForDeploymentsToComplete( targetEnvironment, "Previous deployment in progress" );

        PlanResultKey buildPlanResultKey = topBuildContext.getPlanResultKey();
        releaseName = String.format( "%d :: %s :: %s", chainResultsSummary.getBuildNumber(), dep2env, releaseName );
        DeploymentVersion deploymentVersion = deploymentVersionService.getDeploymentVersionByName( releaseName, deploymentProject.getId() );
        if ( deploymentVersion != null ) {
            logg( "Deployment version " + releaseName + " already exists" );
            return;
        }
        try {
            deploymentVersion = deploymentVersionService.createDeploymentVersion( deploymentProject.getId(), buildPlanResultKey );
            deploymentVersionService.renameVersion( deploymentProject.getId(), deploymentVersion, releaseName );
        } catch ( Exception exception ) {
            if ( deploymentVersion == null ) {
                logg( exception.getMessage() );
                return;
            }
        }
        logg( "Deployment version " + releaseName + " created" );

        String username = "undefined";
        if ( topBuildContext.getTriggerReason() instanceof ManualBuildTriggerReason ) {
            username = ((ManualBuildTriggerReason)topBuildContext.getTriggerReason()).getUserName();
        }
        User user = bambooUserManager.getUser( username );

        EnvironmentTriggeringAction environmentTriggeringAction = triggeringActionFactory.createManualEnvironmentTriggeringAction(
            targetEnvironment, deploymentVersion, user
        );

        logg( String.format( "Starting deployment '%s' to %s", releaseName, dep2env ) );
        ExecutionRequestResult executionRequestResult = deploymentExecutionService.execute( targetEnvironment, environmentTriggeringAction );
    }

    private void waitForDeploymentsToComplete(Environment environment, String msg ) {
        while( deploymentExecutionService.isEnvironmentBeingDeployedTo( environment.getId() ) ) {
            try {
                logg( msg + " - delaying 5 seconds" );
                Thread.sleep( 5000 );
            } catch ( InterruptedException e ) {
                logg( "Waiting for deployment error: " + e.getMessage() );
            }
        }
    }
}
