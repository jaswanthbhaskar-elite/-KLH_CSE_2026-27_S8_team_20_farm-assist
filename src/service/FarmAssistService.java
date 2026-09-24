package service;

import algorithms.flow.EdmondsKarp;
import algorithms.recommend.CropKnapsackDP;
import algorithms.search.AhoCorasick;
import algorithms.search.FuzzyMatch;
import algorithms.search.KMP;
import algorithms.search.RabinKarp;
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
 *
 * All searches fall back to typo-tolerant Edit Distance matching
 * (FuzzyMatch) if the exact algorithm finds nothing.
 */
public class FarmAssistService {

    private final List<Crop> crops;
    private final List<Disease> diseases;
    private final List<Fertilizer> fertilizers;
    private final List<Requirement> requirements;
    private final List<Symptom> symptoms;
    private final AhoCorasick symptomMatcher;

    public FarmAssistService(String cropsFile, String diseasesFile, String fertilizersFile,
                              String requirementsFile, String symptomsFile) {
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

    // ---------- Getters ----------

    public List<Crop> getCrops() { return crops; }
    public List<Disease> getDiseases() { return diseases; }
    public List<Fertilizer> getFertilizers() { return fertilizers; }
    public List<Requirement> getRequirements() { return requirements; }
    public List<Symptom> getSymptoms() { return symptoms; }
}