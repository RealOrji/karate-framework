package com.realorji.api.support;

import com.intuit.karate.http.HttpLogModifier;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Masks credentials and tokens in Karate's HTTP logs and HTML reports,
 * so reports can be shared or published from CI without leaking secrets.
 */
public final class SensitiveDataMasker implements HttpLogModifier {

    public static final SensitiveDataMasker INSTANCE = new SensitiveDataMasker();

    private static final String MASK = "*****";
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie", "set-cookie");
    private static final Pattern SENSITIVE_JSON_FIELDS =
            Pattern.compile("(\"(?:password|token)\"\\s*:\\s*\")[^\"]*(\")");

    private SensitiveDataMasker() {
    }

    @Override
    public boolean enableForUri(String uri) {
        return true;
    }

    @Override
    public String uri(String uri) {
        return uri;
    }

    @Override
    public String header(String header, String value) {
        return header != null && SENSITIVE_HEADERS.contains(header.toLowerCase()) ? MASK : value;
    }

    @Override
    public String request(String uri, String request) {
        return mask(request);
    }

    @Override
    public String response(String uri, String response) {
        return mask(response);
    }

    private static String mask(String body) {
        return body == null ? null : SENSITIVE_JSON_FIELDS.matcher(body).replaceAll("$1" + MASK + "$2");
    }
}
