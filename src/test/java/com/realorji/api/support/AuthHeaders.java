package com.realorji.api.support;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Builds HTTP auth header values; exposed to features as {@code AuthHeaders}. */
public final class AuthHeaders {

    private AuthHeaders() {
    }

    /** {@code AuthHeaders.basic('admin', 'secret')} -> "Basic YWRtaW46c2VjcmV0" */
    public static String basic(String username, String password) {
        String raw = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
