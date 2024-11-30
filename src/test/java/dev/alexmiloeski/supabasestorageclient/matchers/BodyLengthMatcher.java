package dev.alexmiloeski.supabasestorageclient.matchers;

import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.MatchResult;

import java.util.function.Predicate;

/**
 * <p>A Matcher class that matches Wiremock requests by their body's size in <b>bytes</b>.</p>
 * <p>You can use either one of the 3 provided comparisons (equals, greater than, less than),
 * or a predicate for custom logic.</p>
 * <p>Examples:</p>
 * <pre>
 * stubFor(post("/some/path")
 *     .withRequestBody(withSizeGreaterThan(10))
 *     .willReturn(badRequest().withBody("body size too large")));
 * </pre>
 * <pre>
 * stubFor(post("/some/path")
 *     .withRequestBody(withSizeMatching(size -> size == 0 || size > 10))
 *     .willReturn(badRequest().withBody("body must be between 1 and 10 bytes"))));
 * </pre>
 */
public class BodyLengthMatcher extends ContentPattern<byte[]> {
    public enum OP { EQUAL, ABOVE, BELOW }

    private final Predicate<Integer> predicate;
    private final OP operation;
    private final int comparedTo;

    public BodyLengthMatcher(Predicate<Integer> predicate) {
        super(new byte[0]);
        this.predicate = predicate;
        this.operation = null;
        this.comparedTo = 0;
    }
    public BodyLengthMatcher(OP operation, int comparedTo) {
        super(new byte[0]);
        this.predicate = null;
        this.operation = operation;
        this.comparedTo = comparedTo;
    }

    @Override
    public MatchResult match(byte[] value) {
        boolean isMatch;
        if (predicate != null) {
            isMatch = predicate.test(value.length);
        } else {
            isMatch = switch (operation) {
                case EQUAL -> value.length == comparedTo;
                case ABOVE -> value.length > comparedTo;
                case BELOW -> value.length < comparedTo;
            };
        }
        return MatchResult.of(isMatch);
    }

    @Override
    public String getName() {
        return "bodyLength";
    }

    @Override
    public String getExpected() {
        if (predicate != null) {
            return "match a given predicate";
        }
        return switch (operation) {
            case EQUAL -> "equal to " + comparedTo;
            case ABOVE -> "greater than " + comparedTo;
            case BELOW -> "less than " + comparedTo;
        };
    }

    /**
     * Matches requests whose body's size fits the predicate. Example:
     * <pre>
     * stubFor(post("/some/path")
     *     .withRequestBody(withSizeMatching(size -> size == 0 || size > 10))
     *     .willReturn(badRequest().withBody("body must be between 1 and 10 bytes"))));
     * </pre>
     * @param predicate A lambda with an Integer input that represents the body's size
     */
    public static BodyLengthMatcher withSizeMatching(Predicate<Integer> predicate) {
        return new BodyLengthMatcher(predicate);
    }

    public static BodyLengthMatcher withSizeEqualTo(int size) {
        return new BodyLengthMatcher(BodyLengthMatcher.OP.EQUAL, size);
    }

    public static BodyLengthMatcher withSizeGreaterThan(int size) {
        return new BodyLengthMatcher(BodyLengthMatcher.OP.ABOVE, size);
    }

    public static BodyLengthMatcher withSizeLessThan(int size) {
        return new BodyLengthMatcher(BodyLengthMatcher.OP.BELOW, size);
    }
}
