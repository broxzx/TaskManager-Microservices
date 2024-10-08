package com.project.projectservice.utils;

public class SecurityUtils {

    public static final String mockedAuthorizationHeaderWithUserRole = "Bearer some_user_token";

    public static final String mockedTokenWithUserRole = mockedAuthorizationHeaderWithUserRole.substring(7);


    public static final String mockedAuthorizationHeaderWithAdminRole = "Bearer some_admin_token";

    public static final String mockedTokenWithAdminRole = mockedAuthorizationHeaderWithAdminRole.substring(7);


}
