package algorithms.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Knuth-Morris-Pratt substring search.
 * Case-insensitive: both text and pattern are lowercased before matching.
 *
 * Time complexity : O(n + m)   n = text length, m = pattern length
 * Space complexity: O(m)       for the LPS array
 */
public class KMP {

    /**
     * Builds the LPS (Longest Proper Prefix which is also Suffix) array.
     * lps[i] = length of the longest proper prefix of pattern[0..i]
     *          that is also a suffix of pattern[0..i].
     */
    private static int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0; // length of the previous longest prefix-suffix
        int i = 1;

        lps[0] = 0; // a single character has no proper prefix/suffix
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else if (len != 0) {
                // fall back to the previous known prefix-suffix length
                // instead of starting over from 0 — this is the key
                // amortization step that keeps LPS construction O(m).
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    /**
     * Returns every starting index in `text` where `pattern` occurs.
     * Case-insensitive. Returns an empty list if pattern is empty
     * or not found.
     */
    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();
        if (text == null || pattern == null || pattern.isEmpty()) {
            return matches;
        }

        String t = text.toLowerCase();
        String p = pattern.toLowerCase();
        int n = t.length();
        int m = p.length();
        if (m > n) return matches;

        int[] lps = buildLPS(p);

        int i = 0; // pointer into text  -- never moves backward
        int j = 0; // pointer into pattern
        while (i < n) {
            if (t.charAt(i) == p.charAt(j)) {
                i++;
                j++;
                if (j == m) {
                    matches.add(i - j); // full match found
                    j = lps[j - 1];      // continue looking for overlapping matches
                }
            } else if (j != 0) {
                j = lps[j - 1]; // jump using LPS instead of resetting to 0
            } else {
                i++;
            }
        }
        return matches;
    }

    /** Convenience method: does the pattern occur anywhere in the text? */
    public static boolean contains(String text, String pattern) {
        return !search(text, pattern).isEmpty();
    }
}