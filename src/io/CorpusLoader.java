package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the plain-text agricultural corpus used to demonstrate the
 * string-matching algorithms in algorithms.search (KMP, RabinKarp,
 * AhoCorasick) on a realistically sized body of text.
 *
 * Why this exists: before this class, KMP/RabinKarp/AhoCorasick were
 * only ever run against tiny per-record strings built by
 * FarmAssistService (e.g. one crop's name + soil type + season, a
 * handful of words). That is not enough text to meaningfully show why
 * linear-time substring search algorithms matter. CorpusLoader instead
 * reads a much larger multi-paragraph text file
 * (data/agricultural_corpus.txt) so the SAME, unmodified search
 * algorithms can be exercised over a realistic amount of text.
 *
 * This class is intentionally separate from io.DataLoader:
 * DataLoader parses small, structured "|"-delimited rows into typed
 * model objects (Crop, Disease, Fertilizer, ...). CorpusLoader instead
 * reads free-form prose with no delimiters or fixed columns, so it
 * does not fit DataLoader's row-parsing methods and is not something
 * DataLoader could reasonably be extended to do. Both classes still
 * follow the same simple pattern: plain static methods, a
 * BufferedReader over a file path, and graceful handling of a missing
 * or unreadable file (an empty result instead of crashing the app).
 */
public class CorpusLoader {

    /**
     * Reads every line of the corpus file into a list, in order.
     * Blank lines are kept so the text keeps its original paragraph
     * structure when re-joined by loadCorpusText().
     */
    public static List<String> loadCorpusLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error loading corpus file: " + e.getMessage());
        }
        return lines;
    }

    /**
     * Reads the whole corpus file into a single String, ready to be
     * handed to KMP.search / RabinKarp.search / AhoCorasick.search as
     * the "text" to scan for a pattern.
     *
     * Lines are joined with a single space rather than a newline. The
     * corpus file is hand-written prose that is word-wrapped onto
     * multiple lines for readability, so a multi-word search phrase
     * (e.g. "drip irrigation") can end up split across two lines; a
     * space keeps such phrases searchable as one continuous piece of
     * text while still separating word boundaries correctly.
     */
    public static String loadCorpusText(String filePath) {
        List<String> lines = loadCorpusLines(filePath);
        return String.join(" ", lines);
    }
}