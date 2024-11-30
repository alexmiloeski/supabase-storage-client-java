package dev.alexmiloeski.supabasestorageclient.matchers;

import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.MatchResult;

import java.util.function.Predicate;

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
