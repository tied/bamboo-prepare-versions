package com.github.bayaro;

import java.util.*;

public class KnownEnvironmentBuilds {

    private String name;
    final Map<String, KnownProjectBuilds> projects = new TreeMap<>();

    public KnownEnvironmentBuilds( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, KnownProjectBuilds> getProjects() {
        return projects;
    }

    public KnownProjectBuilds getProject( String project ) {
        return projects.get( project );
    }

    public KnownProjectBuilds addProject( String project ) {
        KnownProjectBuilds builds = projects.get( project );
        if ( builds == null ) {
            projects.put( project, new KnownProjectBuilds( project ) );
            builds = projects.get( project );
        }
        return builds;
    }

    public void addVersion( String project, String branch, String version ) {
        addProject( project ).addVersion( branch, version );
    }
}
