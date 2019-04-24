package com.github.bayaro;

import java.util.*;

public class KnownProjectBuilds {

    private String name;
    final Map<String, List<String>> branches = new LinkedHashMap<>();

    public KnownProjectBuilds( String name ) {
        this.name = name;
        addVersion( "-", "present" );
        addVersion( "-", "latest" );
    }

    public String getName() {
        return name;
    }

    public Map<String, List<String>> getBranches() {
        return branches;
    }

    public List<String> getBranch( String branch ) {
        return branches.get( branch );
    }

    public void addVersion( String branch, String version ) {
        List<String> b = branches.get( branch );
        if ( b == null ) {
            branches.put( branch, new ArrayList<String>() );
            b = branches.get( branch );
        }
        b.add( version );
    }
}
