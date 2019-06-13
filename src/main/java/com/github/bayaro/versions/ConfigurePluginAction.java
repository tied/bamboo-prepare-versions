package com.github.bayaro.versions;

import com.atlassian.bamboo.bandana.PlanAwareBandanaContext;
import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.atlassian.bandana.BandanaManager;
import java.util.List;

public class ConfigurePluginAction extends GlobalAdminAction {

    public static final String BANDANA_KEY_STORAGE_URL = "custom.bamboo.prepare-versions.storage-url";
    public static final String BANDANA_KEY_STORAGE_USR = "custom.bamboo.prepare-versions.storage-usr";
    public static final String BANDANA_KEY_STORAGE_PWD = "custom.bamboo.prepare-versions.storage-pwd";
    private BandanaManager bandanaManager;

    private String storageUrl = "";
    private String storageUsr = "";
    private String storagePwd = "";
    private boolean success = false;
    private String errorMessage = "Unknown Error";

    public String input() {
        storageUrl = String.valueOf( bandanaManager.getValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, BANDANA_KEY_STORAGE_URL ) );
        storageUsr = String.valueOf( bandanaManager.getValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, BANDANA_KEY_STORAGE_USR ) );
        storagePwd = String.valueOf( bandanaManager.getValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, BANDANA_KEY_STORAGE_PWD ) );
        return INPUT;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String save() {
        List<String> lines = PrepareVersionsForm.loadURL( "", storageUrl, storageUsr, storagePwd );
        if ( lines.size() == 0 ) {
            return ERROR;
        }
        if ( lines.get( 0 ) == "ERROR" ) {
            this.errorMessage = String.join( "<br>", lines );
            return ERROR;
        }
        this.success = true;
        bandanaManager.setValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, BANDANA_KEY_STORAGE_URL, storageUrl );
        bandanaManager.setValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, BANDANA_KEY_STORAGE_USR, storageUsr );
        bandanaManager.setValue( PlanAwareBandanaContext.GLOBAL_CONTEXT, BANDANA_KEY_STORAGE_PWD, storagePwd );
        return SUCCESS;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public void setBandanaManager(BandanaManager bandanaManager) {
        this.bandanaManager = bandanaManager;
    }

    public String getStorageUrl() {
        return storageUrl;
    }
    public void setStorageUrl( String storageUrl ) {
        this.storageUrl = storageUrl;
    }

    public String getStorageUsr() {
        return storageUsr;
    }
    public void setStorageUsr( String storageUsr ) {
        this.storageUsr = storageUsr;
    }

    public String getStoragePwd() {
        return storagePwd;
    }
    public void setStoragePwd( String storagePwd ) {
        this.storagePwd = storagePwd;
    }
}
