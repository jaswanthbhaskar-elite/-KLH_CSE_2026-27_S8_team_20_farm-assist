package models;

/**
 * One row from symptoms.txt: a known symptom phrase and the disease
 * it is associated with. Used as the pattern dictionary for
 * Aho-Corasick multi-symptom searching.
 */
public class Symptom {
    private final String phrase;
    private final String diseaseName;

    public Symptom(String phrase, String diseaseName) {
        this.phrase = phrase;
        this.diseaseName = diseaseName;
    }

    public String getPhrase() { return phrase; }
    public String getDiseaseName() { return diseaseName; }

    @Override
    public String toString() {
        return String.format("%s -> %s", phrase, diseaseName);
    }
}
