package com.github.bayaro.versions;

import com.opensymphony.xwork2.Action;
import com.atlassian.bamboo.ww2.*;

class CommitsList extends BambooActionSupport {

    @Override
    public String doDefault() throws Exception {
        return Action.SUCCESS;
	}
}
