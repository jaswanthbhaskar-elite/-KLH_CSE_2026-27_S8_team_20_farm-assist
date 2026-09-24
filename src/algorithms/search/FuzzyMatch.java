package algorithms.search;

/**
 * Typo-tolerant fuzzy matching using Edit Distance (Levenshtein) DP.
 *
 * Used as a FALLBACK after exact KMP / Rabin-Karp search fails, so a query
 * like "yello spots" can still find a record containing "Yellow spots".
 *
 * Case-insensitive (like KMP and RabinKarp): everything is lowercased
 * before comparison.
 *
 * DP state: dp[i][j] = min edits to convert a[0..i-1] into b[0..j-1]
 * Time complexity  : O(n * m) per word pair   (n, m = word lengths)
 * Space complexity : O(n * m) per word pair
 */
public class FuzzyMatch {

    /**
     * Classic Levenshtein edit distance DP.
     * Returns the minimum number of insert/delete/substitute operations
     * needed to turn `a` into `b`.
     */
    private static int editDistance(String a, String b) {
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];

        // Base cases: converting to/from an empty string.
        for (int i = 0; i <= n; i++) dp[i][0] = i; // delete all i characters
        for (int j = 0; j <= m; j++) dp[0][j] = j; // insert all j characters

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // characters match, no edit
                } else {
                    dp[i][j] = 1 + Math.min(
                            dp[i - 1][j],       // delete
                            Math.min(
                                    dp[i][j - 1],     // insert
                                    dp[i - 1][j - 1]  // substitute
                            )
                    );
                }
            }
        }
        return dp[n][m];
    }

    /**
     * How many typos are we willing to tolerate for a word of this length?
     * Short words get 0 tolerance (otherwise "cat" would fuzzy-match "cot",
     * "cop", "bat", etc. and produce too many false positives).
     */
    private static int maxAllowedDistance(int wordLength) {
        if (wordLength <= 3) return 0;
        if (wordLength <= 6) return 1;
        return 2;
    }

    /**
     * Returns true if EVERY word in `query` fuzzy-matches at least one word
     * in `text` (within the length-based edit-distance tolerance).
     * This mirrors how a person would judge "close enough": every word the
     * user typed should correspond to something in the record.
     */
    public static boolean matches(String text, String query) {
        if (text == null || query == null || query.isBlank()) {
            return false;
        }

        String[] textWords = text.toLowerCase().split("[^a-z0-9]+");
        String[] queryWords = query.toLowerCase().split("[^a-z0-9]+");

        for (String qWord : queryWords) {
            if (qWord.isEmpty()) continue;

            boolean found = false;
            int allowed = maxAllowedDistance(qWord.length());

            for (String tWord : textWords) {
                if (tWord.isEmpty()) continue;
                if (editDistance(qWord, tWord) <= allowed) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false; // this query word had no close-enough match anywhere
            }
        }
        return true;
    }
}
