package me.leoko.advancedban.utils;

import java.util.regex.Pattern;

/**
 * @author <a href="https://github.com/iGabyTM">GabyTM</a>
 */
public enum Regex {

    DIGITS("\\d+"),
    DIGITS_OR_X("\\d+|X"),
    IP("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"),
    REASON_LAYOUT("[~@].+"),
    TIME_LAYOUT("#.*");

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
        DOUBLE_QUOTE("\""),
        SINGLE_QUOTE("'");

        private final Pattern pattern;

        Replace(final String regex) {
            this.pattern = Pattern.compile(regex);
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

        /**
         * Source <a href="http://stackoverflow.com/a/8270824">http://stackoverflow.com/a/8270824</a>
         */
        LETTERS_AND_DIGITS("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)"),
        SPACE(" ");

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
