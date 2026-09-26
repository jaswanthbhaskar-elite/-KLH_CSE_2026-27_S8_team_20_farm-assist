package service;

import algorithms.flow.EdmondsKarp;
import algorithms.recommend.CropKnapsackDP;
import algorithms.search.AhoCorasick;
import algorithms.search.FuzzyMatch;
import algorithms.search.KMP;
import algorithms.search.RabinKarp;
import io.CorpusLoader;
import io.DataLoader;
import models.Crop;
import models.Disease;
import models.Fertilizer;
import models.Requirement;
import models.Symptom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central service layer for Farm Assist.
 * Owns the in-memory agricultural data and exposes search, recommendation,
 * allocation, and multi-symptom search operations. Each feature has a
 * FIXED algorithm assignment (no user choice):
 *
 *   Crop Information Search       -> KMP
 *   Disease Information Search    -> Rabin-Karp
 *   Fertilizer Information Search -> simple linear lookup (contains)
 *   Multi-Symptom Disease Search  -> Aho-Corasick
 *   Crop Recommendation           -> 0/1 Knapsack Dynamic Programming
 *   Fertilizer Allocation         -> Edmonds-Karp Max Flow
 *   Agricultural Corpus Search    -> KMP + Rabin-Karp (single term),
 *                                    Aho-Corasick (multiple terms at once)
 *
 * All searches fall back to typo-tolerant Edit Distance matching
 * (FuzzyMatch) if the exact algorithm finds nothing.
 *
 * The searches above (crop/disease/fertilizer) all run over tiny,
 * per-record strings (one crop's name + soil type + season, etc).
 * Agricultural Corpus Search instead runs the SAME KMP, Rabin-Karp and
 * Aho-Corasick implementations over a much larger block of text loaded
 * by io.CorpusLoader from data/agricultural_corpus.txt, so the
 * algorithms can be demonstrated on a realistic amount of data.
 */
public class FarmAssistService {

    /** Default location of the larger text corpus (see io.CorpusLoader). */
    private static final String DEFAULT_CORPUS_FILE = "data/agricultural_corpus.txt";

    private final List<Crop> crops;
    private final List<Disease> diseases;
    private final List<Fertilizer> fertilizers;
    private final List<Requirement> requirements;
    private final List<Symptom> symptoms;
    private final AhoCorasick symptomMatcher;

    // The larger agricultural corpus, loaded once at startup by CorpusLoader.
    private final List<String> corpusLines;
    private final String corpusText;

    public FarmAssistService(String cropsFile, String diseasesFile, String fertilizersFile,
                              String requirementsFile, String symptomsFile) {
        this(cropsFile, diseasesFile, fertilizersFile, requirementsFile, symptomsFile, DEFAULT_CORPUS_FILE);
    }

    public FarmAssistService(String cropsFile, String diseasesFile, String fertilizersFile,
                              String requirementsFile, String symptomsFile, String corpusFile) {
        this.crops = DataLoader.loadCrops(cropsFile);
        this.diseases = DataLoader.loadDiseases(diseasesFile);
        this.fertilizers = DataLoader.loadFertilizers(fertilizersFile);
        this.requirements = DataLoader.loadRequirements(requirementsFile);
        this.symptoms = DataLoader.loadSymptoms(symptomsFile);

        // Build the Aho-Corasick trie once at startup from every known
        // symptom phrase, so multi-symptom search reuses the same
        // constructed automaton for every query.
        this.symptomMatcher = new AhoCorasick();
        for (Symptom s : symptoms) {
            symptomMatcher.addPattern(s.getPhrase().toLowerCase());
        }
        symptomMatcher.build();

        // Load the larger agricultural corpus once at startup, via
        // CorpusLoader, so it is ready for searchCorpus()/
        // searchCorpusMultiTerm() without re-reading the file per query.
        this.corpusLines = CorpusLoader.loadCorpusLines(corpusFile);
        this.corpusText = CorpusLoader.loadCorpusText(corpusFile);
    }

    // ---------- Crop Information Search (KMP, fixed) ----------

    public List<Crop> searchCrops(String query) {
        List<Crop> results = new ArrayList<>();
        for (Crop c : crops) {
            String searchableText = String.join(" ",
                    c.getName(), c.getSoilType(), c.getWaterRequirement(), c.getSeason());
            if (KMP.contains(searchableText, query) || FuzzyMatch.matches(searchableText, query)) {
                results.add(c);
            }
        }
        return results;
    }

    // ---------- Disease Information Search (Rabin-Karp, fixed) ----------

    public List<Disease> searchDiseases(String query) {
        List<Disease> results = new ArrayList<>();
        for (Disease d : diseases) {
            String searchableText = String.join(" ",
                    d.getName(), d.getSymptoms(), d.getTreatment());
            if (RabinKarp.contains(searchableText, query) || FuzzyMatch.matches(searchableText, query)) {
                results.add(d);
            }
        }
        return results;
    }

    // ---------- Fertilizer Information Search (simple lookup, fixed) ----------

    public List<Fertilizer> searchFertilizers(String query) {
        List<Fertilizer> results = new ArrayList<>();
        for (Fertilizer f : fertilizers) {
            String searchableText = String.join(" ", f.getName(), f.getUsedFor());
            if (containsIgnoreCase(searchableText, query) || FuzzyMatch.matches(searchableText, query)) {
                results.add(f);
            }
        }
        return results;
    }

    /**
     * Plain case-insensitive substring lookup. No specialized string
     * matching algorithm is used here on purpose: the fertilizer
     * dataset is small, so a dedicated pattern-matching algorithm
     * would add complexity without a meaningful performance or
     * functional benefit (this is the same reasoning documented in
     * the project's design principles: don't force an algorithm in
     * where it isn't genuinely needed).
     */
    private boolean containsIgnoreCase(String text, String query) {
        if (text == null || query == null || query.isBlank()) {
            return false;
        }
        return text.toLowerCase().contains(query.toLowerCase());
    }

    // ---------- Multi-Symptom Disease Search (Aho-Corasick, fixed) ----------

    /** One ranked disease result: how many of the farmer's described symptoms matched it. */
    public static class DiseaseMatch {
        public final String diseaseName;
        public final int matchCount;

        public DiseaseMatch(String diseaseName, int matchCount) {
            this.diseaseName = diseaseName;
            this.matchCount = matchCount;
        }
    }

    /** Full result of a multi-symptom search: which phrases matched, and ranked disease candidates. */
    public static class SymptomSearchResult {
        public final List<String> matchedSymptoms;
        public final List<DiseaseMatch> rankedDiseases;

        public SymptomSearchResult(List<String> matchedSymptoms, List<DiseaseMatch> rankedDiseases) {
            this.matchedSymptoms = matchedSymptoms;
            this.rankedDiseases = rankedDiseases;
        }
    }

    /**
     * Searches the farmer's free-text symptom description for every
     * known symptom phrase SIMULTANEOUSLY using Aho-Corasick, then
     * maps matched phrases back to diseases and ranks diseases by
     * how many of their known symptoms were mentioned.
     */
    public SymptomSearchResult searchBySymptoms(String query) {
        List<String> matchedPhrases = symptomMatcher.search(query == null ? "" : query);

        // Count how many matched phrases belong to each disease.
        Map<String, Integer> diseaseCounts = new LinkedHashMap<>();
        for (String phrase : matchedPhrases) {
            for (Symptom s : symptoms) {
                if (s.getPhrase().equalsIgnoreCase(phrase)) {
                    diseaseCounts.merge(s.getDiseaseName(), 1, Integer::sum);
                }
            }
        }

        List<DiseaseMatch> ranked = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : diseaseCounts.entrySet()) {
            ranked.add(new DiseaseMatch(entry.getKey(), entry.getValue()));
        }
        // Highest match count first.
        ranked.sort((a, b) -> b.matchCount - a.matchCount);

        return new SymptomSearchResult(matchedPhrases, ranked);
    }

    // ---------- Crop Recommendation API (Dynamic Programming) ----------

    public CropKnapsackDP.Result recommendCrops(int landCapacity, int budgetCapacity, int waterCapacity) {
        return CropKnapsackDP.recommend(crops, landCapacity, budgetCapacity, waterCapacity);
    }

    // ---------- Fertilizer Allocation API (Edmonds-Karp Max Flow) ----------

    public EdmondsKarp.AllocationResult allocateFertilizers() {
        return EdmondsKarp.allocate(fertilizers, requirements);
    }

    // ---------- Agricultural Corpus Search (KMP + Rabin-Karp + Aho-Corasick demo) ----------

    /**
     * Result of searching the larger agricultural corpus for a single
     * term. Both KMP and Rabin-Karp are run over the SAME corpus text
     * on purpose, so their match positions can be compared directly —
     * both are correct exact-substring algorithms, so they should
     * always report the same positions, just by different routes
     * (LPS-table skipping vs. rolling hash + verification).
     */
    public static class CorpusSearchResult {
        public final String term;
        public final List<Integer> kmpPositions;
        public final List<Integer> rabinKarpPositions;
        public final List<String> snippets;

        public CorpusSearchResult(String term, List<Integer> kmpPositions,
                                   List<Integer> rabinKarpPositions, List<String> snippets) {
            this.term = term;
            this.kmpPositions = kmpPositions;
            this.rabinKarpPositions = rabinKarpPositions;
            this.snippets = snippets;
        }
    }

    /**
     * Searches the full agricultural corpus (loaded by CorpusLoader)
     * for one term, using the EXISTING KMP and Rabin-Karp
     * implementations — unmodified — over a much larger amount of text
     * than searchCrops()/searchDiseases() ever run them on.
     */
    public CorpusSearchResult searchCorpus(String term) {
        if (term == null || term.isBlank()) {
            return new CorpusSearchResult(term, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        List<Integer> kmpPositions = KMP.search(corpusText, term);
        List<Integer> rabinKarpPositions = RabinKarp.search(corpusText, term);
        List<String> snippets = extractSnippets(kmpPositions, term);
        return new CorpusSearchResult(term, kmpPositions, rabinKarpPositions, snippets);
    }

    /**
     * Searches the full agricultural corpus for several terms
     * SIMULTANEOUSLY in one pass, using the EXISTING Aho-Corasick
     * implementation — the same multi-pattern use case it already
     * serves for symptomMatcher, applied here to the larger corpus
     * instead of the short list of known symptom phrases.
     */
    public List<String> searchCorpusMultiTerm(List<String> terms) {
        AhoCorasick corpusMatcher = new AhoCorasick();
        for (String term : terms) {
            if (term != null && !term.isBlank()) {
                corpusMatcher.addPattern(term.trim().toLowerCase());
            }
        }
        corpusMatcher.build();
        return corpusMatcher.search(corpusText);
    }

    /** Builds a short line of context around each match position, for display. */
    private List<String> extractSnippets(List<Integer> positions, String term) {
        List<String> snippets = new ArrayList<>();
        int radius = 40;
        for (int pos : positions) {
            int start = Math.max(0, pos - radius);
            int end = Math.min(corpusText.length(), pos + term.length() + radius);
            String snippet = corpusText.substring(start, end).replace("\n", " ").trim();
            snippets.add("..." + snippet + "...");
            if (snippets.size() >= 5) break; // keep demo output readable
        }
        return snippets;
    }

    // ---------- Getters ----------

    public List<Crop> getCrops() { return crops; }
    public List<Disease> getDiseases() { return diseases; }
    public List<Fertilizer> getFertilizers() { return fertilizers; }
    public List<Requirement> getRequirements() { return requirements; }
    public List<Symptom> getSymptoms() { return symptoms; }
    public List<String> getCorpusLines() { return corpusLines; }
    public String getCorpusText() { return corpusText; }
}