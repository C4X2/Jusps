package io.github.c4x2.jusps.http;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Encapsulates retry decision logic, exponential backoff, and retry-after header parsing.
 */
@Slf4j
public class RetryPolicy {

    private final int maxRetries;
    private final Duration initialBackoff;

    public RetryPolicy(int maxRetries, Duration initialBackoff) {
        this.maxRetries = maxRetries;
        this.initialBackoff = initialBackoff != null ? initialBackoff : Duration.ofMillis(500);
    }

    public boolean shouldRetry(int attempt, int statusCode) {
        if (attempt >= maxRetries) {
            return false;
        }
        // Retry on 429 (Rate limited) or 5xx (Server error / gateway timeout)
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    public boolean shouldRetryOnException(int attempt, Throwable throwable) {
        return attempt < maxRetries && (throwable instanceof java.io.IOException);
    }

    public Duration calculateDelay(int attempt, String retryAfterHeader) {
        if (retryAfterHeader != null && !retryAfterHeader.trim().isEmpty()) {
            try {
                long seconds = Long.parseLong(retryAfterHeader.trim());
                if (seconds > 0) {
                    return Duration.ofSeconds(seconds);
                }
            } catch (NumberFormatException ignored) {
                // Ignore parse failures on date formats and fall back to exponential backoff
            }
        }

        // Exponential backoff: base * 2^attempt with slight jitter
        long delayMillis = (long) (initialBackoff.toMillis() * Math.pow(2, attempt));
        long jitter = (long) (Math.random() * 100);
        return Duration.ofMillis(delayMillis + jitter);
    }

    public void sleep(Duration duration) throws InterruptedException {
        if (duration != null && !duration.isNegative() && !duration.isZero()) {
            log.debug("Backing off for {} ms before retrying", duration.toMillis());
            Thread.sleep(duration.toMillis());
        }
    }
}
