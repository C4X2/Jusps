package io.github.c4x2.jusps.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

/**
 * Represents the target USPS API environment (Production or Testing / TEM).
 */
@Getter
@EqualsAndHashCode
public final class UspsEnvironment {

    /**
     * Official USPS Production Environment (https://apis.usps.com).
     */
    public static final UspsEnvironment PRODUCTION = new UspsEnvironment("https://apis.usps.com");

    /**
     * Official USPS Testing Environment for Mailers / Sandbox (https://apis-tem.usps.com).
     */
    public static final UspsEnvironment TESTING = new UspsEnvironment("https://apis-tem.usps.com");

    /**
     * Alias for TESTING.
     */
    public static final UspsEnvironment TEM = TESTING;

    private final String baseUrl;

    private UspsEnvironment(String baseUrl) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl cannot be null").replaceAll("/+$", "");
    }

    /**
     * Creates a custom environment pointing to a specific base URL (e.g. for mock testing or proxying).
     *
     * @param baseUrl the base URL
     * @return the custom UspsEnvironment
     */
    public static UspsEnvironment custom(String baseUrl) {
        return new UspsEnvironment(baseUrl);
    }

    @Override
    public String toString() {
        return "UspsEnvironment{" + baseUrl + "}";
    }
}
