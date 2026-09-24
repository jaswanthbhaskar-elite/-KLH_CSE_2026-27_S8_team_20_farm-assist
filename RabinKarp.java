package algorithms.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Rabin-Karp substring search using a polynomial rolling hash.
 * Case-insensitive: text and pattern are lowercased before hashing.
 *
 * Average time complexity : O(n + m)
 * Worst case time complexity : O(n * m)   (many hash collisions)
 * Space complexity : O(1) extra (excluding the result list)
 */
public class RabinKarp {

    // Base of the polynomial hash. 256 covers the full extended ASCII range,
    // so every character maps to a distinct "digit" value.
    private static final long BASE = 256L;

    // A large prime modulus keeps hash values bounded and reduces collisions.
    private static final long MOD = 1_000_000_007L;

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

        // h = BASE^(m-1) mod MOD
        // This is the "place value" of the leftmost character in a window
        // of length m — needed to remove its contribution when the window slides.
        long h = 1L;
        for (int i = 0; i < m - 1; i++) {
            h = (h * BASE) % MOD;
        }

        // Compute the hash of the pattern and of the first window of text.
        long patternHash = 0L;
        long windowHash = 0L;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * BASE + p.charAt(i)) % MOD;
            windowHash = (windowHash * BASE + t.charAt(i)) % MOD;
        }

        for (int i = 0; i <= n - m; i++) {
            // Hashes match -> candidate. Verify with a real comparison
            // to rule out collisions before accepting the match.
            if (windowHash == patternHash && t.regionMatches(i, p, 0, m)) {
                matches.add(i);
            }

            // Roll the hash forward to the next window: drop t[i], add t[i+m].
            if (i < n - m) {
                windowHash = (windowHash - t.charAt(i) * h % MOD + MOD) % MOD;
                windowHash = (windowHash * BASE + t.charAt(i + m)) % MOD;
            }
        }
        return matches;
    }

    /** Convenience method: does the pattern occur anywhere in the text? */
    public static boolean contains(String text, String pattern) {
        return !search(text, pattern).isEmpty();
    }
}