package me.leoko.advancedban.utils;

import java.util.regex.Pattern;

/**
 * @author <a href="https://github.com/iGabyTM">GabyTM</a>
 */
public enum Regex {

    ZERO_OR_N_DIGITS("[0-9]*"),
    DIGITS("\\d+"),
    DIGITS_OR_X("\\d+|X"),
    IP("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"),
    REASON_LAYOUT("[~@].+"),
    TIME_LAYOUT("#.*"),
    TWO_DIGITS("[1-9][0-9]*");

    private final Pattern pattern;

    Regex(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Check if the input matches the pattern
     *
     * @param input input to check
     * @return true if it matches, otherwise false
     */
    public boolean matches(final CharSequence input) {
        return pattern.matcher(input).matches();
    }

    public enum Replace {

        COLORS("&[a-fA-Fk-oK-OrR0-9]"),
        DASH("-"),
        DOUBLE_QUOTE("\""),
        PLACEHOLDER_COUNT("%COUNT%"),
        PLACEHOLDER_NAME("%NAME%"),
        PLACEHOLDER_PLAYER("%PLAYER%"),
        PLACEHOLDER_REASON("%REASON%"),
        PLACEHOLDER_TIMESTAMP("%TIMESTAMP%"),
        SINGLE_QUOTE("'");

        private final Pattern pattern;

        Replace(final String regex) {
            this.pattern = Pattern.compile(regex);
        }

        /**
         * Replace every part of the input that matches one of the {@link Replace}s with the value
         * at the same index as the {@link Replace}
         *
         * @param input        input from where values will be replaced
         * @param patterns     patterns used
         * @param replacements values
         * @return a string with values set
         */
        public static String replace(final CharSequence input, final Replace[] patterns, final String[] replacements) {
            final int patternsLength = patterns.length;
            final int replacementsLength = replacements.length;

            if (patternsLength != replacementsLength) {
                throw new IllegalArgumentException(
                        String.format(
                                "Patterns and replacements arrays doesn't have the same length (%d vs %d)",
                                patternsLength, replacementsLength)
                );
            }

            String string = input.toString();

            for (int i = 0; i < patternsLength; i++) {
                string = patterns[i].replace(string, replacements[i]);
            }

            return string;
        }

        /**
         * Replace every part of the input that matches the pattern with the given value
         *
         * @param input       input to modify
         * @param replacement value to set
         * @return string
         */
        public String replace(final CharSequence input, final String replacement) {
            return pattern.matcher(input).replaceAll(replacement);
        }

        /**
         * Remove every part of the input that matches the pattern (replace with "")
         *
         * @param input input to modify
         * @return string
         */
        public String remove(final CharSequence input) {
            return pattern.matcher(input).replaceAll("");
        }

    }

    public enum Split {

        COLON(":"),
        COMMA(","),
        DOT("\\."),

        /**
         * Source <a href="http://stackoverflow.com/a/8270824">http://stackoverflow.com/a/8270824</a>
         */
        LETTERS_AND_DIGITS("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)"),
        SPACE(" "),
        VERTICAL_LINE("\\|");

        private final Pattern pattern;

        Split(final String regex) {
            this.pattern = Pattern.compile(regex);
        }

        /**
         * Split the given input around matches of this pattern.
         *
         * @param input value to split
         * @return string array
         */
        public String[] split(final CharSequence input) {
            return pattern.split(input);
        }

        /**
         * Split the given input around matches of this pattern.
         *
         * @param input value to split
         * @param limit limit of splits
         * @return string array
         */
        public String[] split(final CharSequence input, final int limit) {
            return pattern.split(input, limit);
        }

    }

}
