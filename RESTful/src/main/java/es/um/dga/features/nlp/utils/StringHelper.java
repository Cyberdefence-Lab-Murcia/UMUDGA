/**
 * Project: DomainNameProfiler
 * Copyright (c) 2017 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Static helper class for String-related algorithms.
 */
public class StringHelper {
    
    /**
     * Dictionary Vowels.
     */
    public static final String VOWELS = "aeiou".toLowerCase();
    
    /**
     * Dictionary Consonants.
     */
    public static final String CONSONANTS = "bcdfghjklmnpqrstvwxyz".toLowerCase();
    
    /**
     * Dictionary Letters.
     */
    public static final String LETTERS = "abcdefghijklmnopqrstuvwxyz".toLowerCase();
    
    /**
     * Dictionary Digits.
     */
    public static final String DIGITS = "0123456789";
    
    /**
     * Dictionary Symbols.
     */
    public static final String SYMBOLS = "-.";
    
    /**
     * Common accepted characters in DNS names. Also accents are supported, but not as a standard.
     */
    public static final String ALPHABET = "-0123456789abcdefghijklmnopqrstuvwxyz".toLowerCase();
    
    /**
     * Longest common substring ('O(mn)' implementation).
     *
     * @param first  First Word.
     * @param second Second Word.
     *
     * @return Length of the longest common substring.
     *
     * @see <a href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Longest_common_substring#Java">WikiBooks Implementation
     * </a>
     */
    public static int longestCommonSubstring(String first, String second) {
        int maxLen = 0;
        int fl = first.length();
        int sl = second.length();
        int[][] table = new int[fl + 1][sl + 1];
        
        for (int i = 1; i <= fl; i++) {
            for (int j = 1; j <= sl; j++) {
                if (Character.toLowerCase(first.charAt(i - 1)) == Character.toLowerCase(second.charAt(j - 1))) {
                    table[i][j] = table[i - 1][j - 1] + 1;
                    if (table[i][j] > maxLen)
                        maxLen = table[i][j];
                }
            }
        }
        return maxLen;
    }
    
    /**
     * Longest consecutive substring in the alphabet.
     * Overload with string param.
     *
     * @param text     Text where to search the longest consecutive substring.
     * @param alphabet Alphabet to search in the Test provided.
     *
     * @return Size of the longest consecutive substring.
     *
     * @see es.um.dga.features.nlp.utils.StringHelper#longestConsecutiveSubstringInAlphabet(char[], String)
     */
    public static int longestConsecutiveSubstringInAlphabet(String text, String alphabet) {
        return longestConsecutiveSubstringInAlphabet(text.toCharArray(), alphabet);
    }
    
    /**
     * Longest consecutive substring in the alphabet.
     *
     * @param text     Text where to search the longest consecutive substring.
     * @param alphabet Alphabet to search in the Test provided.
     *
     * @return Size of the longest consecutive substring.
     */
    public static int longestConsecutiveSubstringInAlphabet(char[] text, String alphabet) {
        int longest = 0;
        int candidateLength = 0;
        for (char c : text) {
            Character C = Character.toLowerCase(c);
            if (alphabet.indexOf(C) >= 0) {
                ++candidateLength;
            }
            else {
                if (longest < candidateLength) {
                    longest = candidateLength;
                }
                candidateLength = 0;
            }
        }
        if (longest < candidateLength) {
            longest = candidateLength;
        }
        return longest;
    }
    
    /**
     * Get the ratio of vowels w.r.t. the text passed as param.
     *
     * @param text Text to be analysed.
     *
     * @return Ratio vowels/text.
     */
    public static Double getRatioVowelsChars(String text) {
        return getRatioOfSymbolsOccurrence(text, VOWELS);
    }
    
    /**
     * Get the ratio of consonants w.r.t. the text passed as param.
     *
     * @param text Text to be analysed.
     *
     * @return Ratio consonants/text.
     */
    public static Double getRatioConsonantsChars(String text) {
        return getRatioOfSymbolsOccurrence(text, CONSONANTS);
    }
    
    /**
     * Get the ratio of letters w.r.t. the text passed as param.
     *
     * @param text Text to be analysed.
     *
     * @return Ratio letters/text.
     */
    public static Double getRatioLettersChars(String text) {
        return getRatioOfSymbolsOccurrence(text, LETTERS);
    }
    
    /**
     * Get the ratio of numbers w.r.t. the text passed as param.
     *
     * @param text Text to be analysed.
     *
     * @return Ratio numbers/text.
     */
    public static Double getRatioNumericChars(String text) {
        return getRatioOfSymbolsOccurrence(text, DIGITS);
    }
    
    /**
     * Get the ratio of symbols w.r.t. the text passed as param.
     *
     * @param text Text to be analysed.
     *
     * @return Ratio symbols/text.
     */
    public static Double getRatioSymbolChars(String text) {
        return getRatioOfSymbolsOccurrence(text, SYMBOLS);
    }
    
    @NotNull private static Double getRatioOfSymbolsOccurrence(String text, String symbols) {
        try {
            double counter = 0;
            for (Character character : text.toCharArray()) {
                if (StringUtils.containsIgnoreCase(symbols, character.toString())) {
                    ++counter;
                }
            }
            return counter / text.length();
        }
        catch (ArithmeticException ex) {
            return Double.NaN;
        }
    }
}
