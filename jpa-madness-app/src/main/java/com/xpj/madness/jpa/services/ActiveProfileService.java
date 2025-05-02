package com.xpj.madness.jpa.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ActiveProfileService {

    public static final String PROFILE_DEFAULT = "";
    public static final String PROFILE_POSTGRES = "postgres";
    public static final String PROFILE_MSSQL = "mssql";

    private final String activeProfile;

    public ActiveProfileService(@Value("${spring.profiles.active}") String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public boolean isDefault() {
        return PROFILE_DEFAULT.equals(activeProfile);
    }

    public boolean isPostgres() {
        return PROFILE_POSTGRES.equals(activeProfile);
    }

    public boolean isMssql() {
        return PROFILE_MSSQL.equals(activeProfile);
    }


}
