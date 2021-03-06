package com.checkmarx.teamcity.server;

import com.checkmarx.teamcity.common.CxConstants;
import com.checkmarx.teamcity.common.CxParam;
import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.StatefulObject;
import jetbrains.buildServer.controllers.admin.projects.BuildTypeForm;
import jetbrains.buildServer.controllers.admin.projects.EditRunTypeControllerExtension;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public class CxEditRunTypeControllerExtension implements EditRunTypeControllerExtension {
    private final CxAdminConfig cxAdminConfig;

    public CxEditRunTypeControllerExtension(@NotNull final SBuildServer server,
                                            @NotNull final CxAdminConfig cxAdminConfig) {
        server.registerExtension(EditRunTypeControllerExtension.class, CxConstants.RUNNER_TYPE, this);

        this.cxAdminConfig = cxAdminConfig;
    }

    public void fillModel(@NotNull final HttpServletRequest request,
                          @NotNull final BuildTypeForm form,
                          @NotNull final Map model) {

        final Map<String, String> properties = form.getBuildRunnerBean().getPropertiesBean().getProperties();


        //put default project name as the build name
        if(StringUtils.isEmpty(properties.get(CxParam.PROJECT_NAME))) {
            properties.put(CxParam.PROJECT_NAME, form.getName());
        }

        //put all global properties to the config page
        for (String conf : CxParam.GLOBAL_CONFIGS) {
            properties.put(conf, cxAdminConfig.getConfiguration(conf));
        }

        model.put(CxParam.USE_DEFAULT_SERVER, properties.get(CxParam.USE_DEFAULT_SERVER));
        model.put(CxParam.SERVER_URL, properties.get(CxParam.SERVER_URL));
        model.put(CxParam.USERNAME, properties.get(CxParam.USERNAME));
        model.put(CxParam.PASSWORD, properties.get(CxParam.PASSWORD));
        model.put(CxParam.GLOBAL_SERVER_URL, cxAdminConfig.getConfiguration(CxParam.GLOBAL_SERVER_URL));
        model.put(CxParam.GLOBAL_USERNAME, cxAdminConfig.getConfiguration(CxParam.GLOBAL_USERNAME));
        model.put(CxParam.GLOBAL_PASSWORD, cxAdminConfig.getConfiguration(CxParam.GLOBAL_PASSWORD));

    }

    public void updateState(@NotNull final HttpServletRequest request, @NotNull final BuildTypeForm form) {}

    @Nullable
    public StatefulObject getState(@NotNull final HttpServletRequest request, @NotNull final BuildTypeForm form) {
        return null;
    }

    public void updateBuildType(@NotNull final HttpServletRequest request,
                                @NotNull final BuildTypeForm form,
                                @NotNull final BuildTypeSettings buildTypeSettings,
                                @NotNull final ActionErrors errors) {}

    @NotNull
    public ActionErrors validate(@NotNull final HttpServletRequest request, @NotNull final BuildTypeForm form) {
        final Map<String, String> properties = form.getBuildRunnerBean().getPropertiesBean().getProperties();
        String cxPass = properties.get(CxParam.PASSWORD);

        try {
            if(cxPass != null && !EncryptUtil.isScrambled(cxPass)) {
                cxPass = EncryptUtil.scramble(cxPass);
            }
        } catch (RuntimeException e) {
            cxPass = "";
        }
        properties.put(CxParam.PASSWORD, cxPass);

        return new ActionErrors();
    }
}
