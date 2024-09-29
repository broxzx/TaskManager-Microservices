package com.project.projectservice.utils;

public class SecurityUtils {

    public static final String mockedAuthorizationHeader = "Bearer some_token";

    public static final String mockedToken = mockedAuthorizationHeader.substring(7);

}
