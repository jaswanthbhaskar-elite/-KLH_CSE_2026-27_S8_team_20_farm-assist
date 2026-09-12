package algorithms.search;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Aho-Corasick multi-pattern string matching.
 *
 * Unlike KMP (single pattern, prefix/suffix table) and Rabin-Karp
 * (single pattern, rolling hash), Aho-Corasick searches for MANY
 * patterns simultaneously in a single pass over the text. It does
 * this by building a Trie of all patterns, then adding "failure
 * links" (conceptually the same idea as KMP's LPS array, but
 * generalized from a single pattern to an entire trie of patterns).
 *
 * Used here for: matching multiple known symptom phrases against a
 * farmer's free-text symptom description in one pass, instead of
 * running a separate single-pattern search once per known symptom.
 *
 * Construction (build once, reused for every search):
 *   1. Insert every pattern into a Trie.               O(m)  m = total pattern length
 *   2. Build failure links via BFS over the Trie.       O(m)
 *
 * Search (run per query):
 *   Single linear scan of the text, following trie edges when
 *   possible and failure links on mismatch (text pointer never
 *   moves backward — same core idea as KMP).
 *   Time  : O(n + z)   n = text length, z = number of matches
 *   Total with construction: O(n + m + z)
 * Space  : O(m * alphabet-ish) for the trie nodes, O(m) for failure
 *          links and output lists.
 */
public class AhoCorasick {

    /** One node of the trie. */
    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        Node failureLink;
        // Patterns that end exactly at this node OR are reachable via
        // this node's failure-link chain (populated during BFS).
        List<String> outputs = new ArrayList<>();
    }

    private final Node root = new Node();

    /**
     * Adds one pattern to the trie. Call this for every known pattern
     * before calling build().
     */
    public void addPattern(String pattern) {
        Node current = root;
        for (char c : pattern.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new Node());
        }
        current.outputs.add(pattern);
    }

    /**
     * Builds failure links for every node using BFS (level order).
     * Must be called once, after all patterns have been added via
     * addPattern(), and before search() is used.
     *
     * Failure link intuition: if we are at a node representing the
     * string "abc" and the next character doesn't match any child,
     * the failure link tells us the longest proper suffix of "abc"
     * that is also a prefix of SOME pattern in the trie — so we can
     * resume matching from there instead of restarting from the root.
     * This is exactly KMP's LPS idea, generalized to a trie.
     */
    public void build() {
        Queue<Node> queue = new ArrayDeque<>();

        // Depth-1 nodes fail directly to the root.
        for (Node child : root.children.values()) {
            child.failureLink = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char c = entry.getKey();
                Node child = entry.getValue();

                // Follow failure links until we find a node that has
                // a transition on 'c', or we fall back to the root.
                Node fail = current.failureLink;
                while (fail != null && !fail.children.containsKey(c)) {
                    fail = fail.failureLink;
                }
                child.failureLink = (fail == null) ? root : fail.children.get(c);

                // Inherit outputs from the failure link so a shorter
                // pattern ending here (via failure) is also reported.
                child.outputs.addAll(child.failureLink.outputs);

                queue.add(child);
            }
        }
    }

    /**
     * Searches `text` for every pattern added earlier, in a single pass.
     * Case-insensitive (text is lowercased; patterns should be added
     * in lowercase by the caller for consistent matching).
     *
     * Returns the distinct set of patterns found, in the order they
     * first appear in the text.
     */
    public List<String> search(String text) {
        List<String> found = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return found;
        }

        String t = text.toLowerCase();
        Node current = root;

        for (char c : t.toCharArray()) {
            // On mismatch, follow failure links (text pointer never moves back).
            while (current != root && !current.children.containsKey(c)) {
                current = current.failureLink;
            }
            if (current.children.containsKey(c)) {
                current = current.children.get(c);
            }
            for (String pattern : current.outputs) {
                if (!found.contains(pattern)) {
                    found.add(pattern);
                }
            }
        }
        return found;
    }
}
